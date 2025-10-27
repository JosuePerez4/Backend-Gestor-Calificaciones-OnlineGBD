package gestor.calificaciones.gestorcalificaciones.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvException;
import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadResponse;
import gestor.calificaciones.gestorcalificaciones.entities.Course;
import gestor.calificaciones.gestorcalificaciones.entities.Exercise;
import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.entities.StudentCourse;
import gestor.calificaciones.gestorcalificaciones.entities.StudentGrade;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import gestor.calificaciones.gestorcalificaciones.entities.User;
import gestor.calificaciones.gestorcalificaciones.enums.GradeStatus;
import gestor.calificaciones.gestorcalificaciones.repository.CourseRepository;
import gestor.calificaciones.gestorcalificaciones.repository.ExerciseRepository;
import gestor.calificaciones.gestorcalificaciones.repository.StudentCourseRepository;
import gestor.calificaciones.gestorcalificaciones.repository.StudentGradeRepository;
import gestor.calificaciones.gestorcalificaciones.repository.StudentRepository;
import gestor.calificaciones.gestorcalificaciones.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvProcessingService {

    private final CourseRepository courseRepository;
    private final ExerciseRepository exerciseRepository;
    private final StudentRepository studentRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public CsvUploadResponse processCsvFile(MultipartFile file, CsvUploadRequest request, UUID teacherId) {
        List<String> errors = new ArrayList<>();
        
        try {
            // Validar archivo CSV
            if (!isValidCsvFile(file)) {
                errors.add("El archivo debe ser un CSV válido");
                return createErrorResponse(errors);
            }

            // Leer y parsear CSV
            List<String[]> csvData = readCsvFile(file);
            if (csvData.isEmpty()) {
                errors.add("El archivo CSV está vacío");
                return createErrorResponse(errors);
            }

            // Obtener o crear curso
            Course course = getOrCreateCourse(request, teacherId);
            
            // Procesar datos del CSV
            List<String> exerciseNames = extractExerciseNames(csvData.get(0));
            List<StudentData> studentsData = extractStudentsData(csvData);
            
            // Crear ejercicios
            List<Exercise> exercises = createExercises(exerciseNames, course);
            
            // Procesar calificaciones de estudiantes
            processStudentGrades(studentsData, exercises, course);
            
            // Calcular estadísticas
            int totalStudents = studentsData.size();
            int totalExercises = exercises.size();
            
            log.info("CSV procesado exitosamente: {} estudiantes, {} ejercicios", totalStudents, totalExercises);
            
            return CsvUploadResponse.builder()
                    .message("Archivo CSV procesado exitosamente")
                    .courseId(course.getId().toString())
                    .courseName(course.getName())
                    .totalStudents(totalStudents)
                    .totalExercises(totalExercises)
                    .errors(errors)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error procesando archivo CSV", e);
            errors.add("Error procesando archivo: " + e.getMessage());
            // Re-lanzar la excepción para que Spring maneje el rollback correctamente
            throw new RuntimeException("Error procesando CSV: " + e.getMessage(), e);
        }
    }

    private boolean isValidCsvFile(MultipartFile file) {
        return file != null && 
               !file.isEmpty() && 
               file.getOriginalFilename() != null && 
               file.getOriginalFilename().toLowerCase().endsWith(".csv");
    }

    private List<String[]> readCsvFile(MultipartFile file) throws IOException, CsvException {
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream(), "UTF-8"))
                .withSkipLines(0)
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build();
        
        try {
            List<String[]> rows = reader.readAll();
            log.info("Total de filas leídas del CSV: {}", rows.size());
            if (!rows.isEmpty()) {
                log.info("Número de columnas en header: {}", rows.get(0).length);
            }
            return rows;
        } finally {
            reader.close();
        }
    }

    private List<String> extractExerciseNames(String[] header) {
        return Arrays.stream(header)
                .skip(1) // Saltar la primera columna (Student Name)
                .filter(name -> !name.trim().isEmpty())
                .map(name -> name.trim().replaceAll("\\s+", " ")) // Limpiar espacios extra
                .collect(Collectors.toList());
    }

    private List<StudentData> extractStudentsData(List<String[]> csvData) {
        List<StudentData> students = csvData.stream()
                .skip(1) // Saltar el header
                .filter(row -> row.length > 0 && !row[0].trim().isEmpty())
                .map(this::createStudentData)
                .collect(Collectors.toList());
        
        log.info("Total estudiantes extraídos: {}", students.size());
        return students;
    }

    private StudentData createStudentData(String[] row) {
        if (row.length == 0) {
            log.warn("Fila vacía encontrada");
            return new StudentData("", new ArrayList<>());
        }
        
        String studentName = row[0].trim().replaceAll("\\s+", " ");
        
        List<String> grades = Arrays.stream(row)
                .skip(1)
                .map(grade -> grade != null ? grade.trim() : "")
                .collect(Collectors.toList());
        
        log.debug("Estudiante parseado: '{}' - Número de calificaciones: {}", studentName, grades.size());
        
        return new StudentData(studentName, grades);
    }

    private Course getOrCreateCourse(CsvUploadRequest request, UUID teacherId) {
        log.info("DEBUG: CsvProcessingService - getOrCreateCourse llamado con teacherId: {}", teacherId);
        
        Optional<Course> existingCourse = courseRepository.findByCourseCode(request.getCourseCode());
        
        if (existingCourse.isPresent()) {
            log.info("DEBUG: CsvProcessingService - Curso existente encontrado: {}", existingCourse.get().getId());
            return existingCourse.get();
        }
        
        log.info("DEBUG: CsvProcessingService - Buscando profesor con ID: {}", teacherId);
        Optional<Teacher> teacherOptional = teacherRepository.findById(teacherId);
        
        if (teacherOptional.isEmpty()) {
            log.error("DEBUG: CsvProcessingService - Profesor no encontrado con ID: {}", teacherId);
            throw new RuntimeException("Profesor no encontrado");
        }
        
        Teacher teacher = teacherOptional.get();
        log.info("DEBUG: CsvProcessingService - Profesor encontrado: {} - Email: {}", teacher.getName(), teacher.getEmail());
        
        Course course = new Course();
        course.setName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCourseCode(request.getCourseCode());
        course.setTeacher(teacher);
        course.setIsActive(true);
        
        return courseRepository.save(course);
    }

    private List<Exercise> createExercises(List<String> exerciseNames, Course course) {
        List<Exercise> exercises = new ArrayList<>();
        
        for (String exerciseName : exerciseNames) {
            Optional<Exercise> existingExercise = exerciseRepository
                    .findByCourseIdAndName(course.getId(), exerciseName);
            
            if (existingExercise.isPresent()) {
                exercises.add(existingExercise.get());
            } else {
                Exercise exercise = new Exercise();
                exercise.setName(exerciseName);
                exercise.setDescription("Ejercicio: " + exerciseName);
                exercise.setMaxScore(100);
                exercise.setCourse(course);
                exercise.setIsActive(true);
                Exercise saved = exerciseRepository.save(exercise);
                exercises.add(saved);
            }
        }
        
        log.info("Total ejercicios procesados: {}", exercises.size());
        return exercises;
    }

    private void processStudentGrades(List<StudentData> studentsData, List<Exercise> exercises, Course course) {
        log.info("Procesando calificaciones de {} estudiantes con {} ejercicios", 
                studentsData.size(), exercises.size());
        
        // Primero, crear/obtener todos los estudiantes de una vez
        List<StudentGrade> gradesToSave = new ArrayList<>();
        
        for (StudentData studentData : studentsData) {
            // Obtener o crear estudiante
            Student student = getOrCreateStudent(studentData.getName());
            
            // Vincular estudiante al curso si no está vinculado
            linkStudentToCourse(student, course);
            
            // Procesar calificaciones
            for (int i = 0; i < exercises.size() && i < studentData.getGrades().size(); i++) {
                String gradeValue = studentData.getGrades().get(i);
                Exercise exercise = exercises.get(i);
                
                StudentGrade grade = processStudentGradeForBatch(student, exercise, gradeValue);
                if (grade != null) {
                    gradesToSave.add(grade);
                }
            }
        }
        
        // Guardar todas las calificaciones en batch
        if (!gradesToSave.isEmpty()) {
            log.info("Guardando {} calificaciones en batch", gradesToSave.size());
            studentGradeRepository.saveAll(gradesToSave);
        }
        
        log.info("Procesamiento de calificaciones completado");
    }

    private Student getOrCreateStudent(String studentName) {
        log.debug("Buscando/creando estudiante con nombre: [{}]", studentName);
        
        // Buscar estudiante por nombre (en un caso real, usarías email o código único)
        List<Student> existingStudents = studentRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(studentName))
                .collect(Collectors.toList());
        
        if (!existingStudents.isEmpty()) {
            log.debug("Estudiante existente encontrado: {}", existingStudents.get(0).getName());
            return existingStudents.get(0);
        }
        
        // Crear nuevo estudiante
        Student student = new Student();
        student.setName(studentName);
        
        // Generar email: convertir nombre a email válido
        String email = studentName.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // Eliminar caracteres especiales excepto espacios
                .trim()
                .replaceAll("\\s+", ".") // Reemplazar espacios con puntos
                + "@estudiante.com";
        
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode("defaultPassword")); // Hasheando la contraseña
        student.setRole(gestor.calificaciones.gestorcalificaciones.enums.Role.STUDENT);
        student.setCode(UUID.randomUUID().toString().substring(0, 8));
        
        log.debug("Creando nuevo estudiante: nombre={}, email={}", studentName, email);
        return studentRepository.save(student);
    }

    private void linkStudentToCourse(Student student, Course course) {
        if (!studentCourseRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            StudentCourse studentCourse = new StudentCourse();
            studentCourse.setStudent(student);
            studentCourse.setCourse(course);
            studentCourse.setIsActive(true);
            studentCourseRepository.save(studentCourse);
        }
    }

    private StudentGrade processStudentGradeForBatch(Student student, Exercise exercise, String gradeValue) {
        log.debug("Procesando calificación: estudiante={}, ejercicio={}, valor={}", 
                student.getName(), exercise.getName(), gradeValue);
        
        StudentGrade studentGrade = new StudentGrade();
        studentGrade.setStudent(student);
        studentGrade.setExercise(exercise);
        
        // Procesar el valor de la calificación
        GradeStatus status = determineGradeStatus(gradeValue);
        studentGrade.setStatus(status);
        
        if (status == GradeStatus.CORRECT || status == GradeStatus.INCORRECT) {
            try {
                int score = Integer.parseInt(gradeValue.trim());
                studentGrade.setScore(score);
                studentGrade.setSubmittedAt(LocalDateTime.now());
                log.debug("Calificación numérica preparada: {}", score);
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear la calificación: {}", gradeValue);
            }
        } else if (status == GradeStatus.NOT_SUBMITTED) {
            // Si no está entregado, no se establece fecha ni score
            studentGrade.setScore(null);
            studentGrade.setSubmittedAt(null);
            log.debug("Ejercicio no entregado");
        }
        
        return studentGrade;
    }

    private GradeStatus determineGradeStatus(String gradeValue) {
        if (gradeValue == null || gradeValue.trim().isEmpty()) {
            return GradeStatus.NOT_SUBMITTED;
        }
        
        String trimmedValue = gradeValue.trim();
        
        if (trimmedValue.equalsIgnoreCase("Not Submitted")) {
            return GradeStatus.NOT_SUBMITTED;
        }
        
        try {
            int score = Integer.parseInt(trimmedValue);
            if (score >= 80) {
                return GradeStatus.CORRECT;
            } else if (score > 0) {
                return GradeStatus.INCORRECT;
            } else {
                return GradeStatus.INCORRECT;
            }
        } catch (NumberFormatException e) {
            return GradeStatus.PENDING;
        }
    }

    private CsvUploadResponse createErrorResponse(List<String> errors) {
        return CsvUploadResponse.builder()
                .message("Error procesando archivo CSV")
                .errors(errors)
                .success(false)
                .build();
    }

    // Clase auxiliar para manejar datos de estudiantes
    private static class StudentData {
        private final String name;
        private final List<String> grades;
        
        public StudentData(String name, List<String> grades) {
            this.name = name;
            this.grades = grades;
        }
        
        public String getName() { return name; }
        public List<String> getGrades() { return grades; }
    }
}

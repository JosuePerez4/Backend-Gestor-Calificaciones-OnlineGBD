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
        // Leer el contenido completo del archivo en memoria para detectar el separador
        // y luego poder leerlo múltiples veces si es necesario
        byte[] fileBytes = file.getBytes();
        
        // Detectar el separador desde el contenido
        char separator = detectSeparator(fileBytes);
        log.info("Separador detectado: '{}'", separator);
        
        // Crear un nuevo InputStream desde los bytes
        java.io.ByteArrayInputStream byteArrayInputStream = new java.io.ByteArrayInputStream(fileBytes);
        
        CSVReader reader = new CSVReaderBuilder(new InputStreamReader(byteArrayInputStream, "UTF-8"))
                .withSkipLines(0)
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(separator)
                        .withQuoteChar('"')  // Manejar valores con comillas
                        .build())
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
    
    private char detectSeparator(byte[] fileBytes) {
        try {
            // Convertir bytes a string para analizar
            String sample = new String(fileBytes, 0, Math.min(fileBytes.length, 2048), "UTF-8");
            
            // Contar comas y punto y comas en la primera línea
            int commaCount = 0;
            int semicolonCount = 0;
            
            // Obtener solo la primera línea
            int firstLineEnd = sample.indexOf('\n');
            if (firstLineEnd > 0) {
                sample = sample.substring(0, firstLineEnd);
            } else if (firstLineEnd == -1 && sample.indexOf('\r') > 0) {
                firstLineEnd = sample.indexOf('\r');
                sample = sample.substring(0, firstLineEnd);
            }
            
            for (char c : sample.toCharArray()) {
                if (c == ',') commaCount++;
                if (c == ';') semicolonCount++;
            }
            
            // Si hay más punto y comas, usar punto y coma; si no, usar coma
            char detected = (semicolonCount > commaCount) ? ';' : ',';
            log.debug("Detectado separador: {} (comas: {}, punto y comas: {})", detected, commaCount, semicolonCount);
            
            return detected;
        } catch (Exception e) {
            log.warn("Error detectando separador, usando coma por defecto", e);
            return ',';
        }
    }

    private List<String> extractExerciseNames(String[] header) {
        return Arrays.stream(header)
                .skip(1) // Saltar la primera columna (Student Name)
                .filter(name -> {
                    String trimmed = name.trim();
                    // Ignorar columna "Total Grade" si existe
                    return !trimmed.isEmpty() && 
                           !trimmed.equalsIgnoreCase("Total Grade") &&
                           !trimmed.equalsIgnoreCase("\"Total Grade\"");
                })
                .map(name -> {
                    // Remover comillas dobles si existen
                    String cleaned = name.trim().replaceAll("^\"|\"$", "");
                    // Limpiar espacios extra
                    return cleaned.replaceAll("\\s+", " ");
                })
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
        
        // Limpiar el nombre del estudiante
        String studentName = row[0].trim()
                .replaceAll("^\"|\"$", "") // Remover comillas dobles
                .replaceAll("\\s+", " ") // Limpiar espacios extra
                .trim();
        
        // Validar que el nombre no sea demasiado largo (máximo 255 caracteres)
        if (studentName.length() > 255) {
            log.warn("Nombre de estudiante demasiado largo ({} caracteres), truncando: {}", 
                    studentName.length(), studentName);
            studentName = studentName.substring(0, 255).trim();
        }
        
        // Validar que el nombre no contenga datos de calificaciones (error de parsing)
        // Si el nombre contiene números que parecen calificaciones, puede ser un error
        if (studentName.length() > 100 && studentName.matches(".*\\d{2,3}.*\\d{2,3}.*")) {
            log.warn("Nombre sospechoso de contener calificaciones: {}", studentName.substring(0, Math.min(100, studentName.length())));
            // Intentar extraer solo el nombre antes de la primera coma múltiple o patrón extraño
            String[] parts = studentName.split(",\\s*");
            if (parts.length > 1 && parts[0].length() < 100) {
                studentName = parts[0].trim();
                log.info("Nombre corregido a: {}", studentName);
            }
        }
        
        // Obtener las calificaciones, ignorando "Total Grade" si está al final
        List<String> grades = new ArrayList<>();
        for (int i = 1; i < row.length; i++) {
            String grade = row[i] != null ? row[i].trim() : "";
            // Remover comillas dobles si existen
            grade = grade.replaceAll("^\"|\"$", "");
            
            // Ignorar si es "Total Grade" o "NA"
            if (!grade.equalsIgnoreCase("Total Grade") && 
                !grade.equalsIgnoreCase("\"Total Grade\"") &&
                !grade.equalsIgnoreCase("NA")) {
                grades.add(grade);
            }
        }
        
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
        
        // Obtener todos los estudiantes inscritos en el curso para buscar emails
        List<StudentCourse> enrolledStudents = studentCourseRepository.findActiveByCourseId(course.getId());
        log.info("Estudiantes ya inscritos en el curso: {}", enrolledStudents.size());
        
        // Primero, crear/obtener todos los estudiantes de una vez
        List<StudentGrade> gradesToSave = new ArrayList<>();
        
        for (StudentData studentData : studentsData) {
            // Obtener o crear estudiante, buscando email en inscritos
            Student student = getOrCreateStudent(studentData.getName(), course, enrolledStudents);
            
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

    private Student getOrCreateStudent(String studentName, Course course, List<StudentCourse> enrolledStudents) {
        log.debug("Buscando/creando estudiante con nombre: [{}]", studentName);
        
        // Limitar longitud del nombre a 255 caracteres y crear variable final
        final String finalStudentName;
        if (studentName.length() > 255) {
            finalStudentName = studentName.substring(0, 255).trim();
            log.warn("Nombre truncado a 255 caracteres");
        } else {
            finalStudentName = studentName;
        }
        
        // 1. Buscar estudiante por nombre exacto (case-insensitive)
        final String searchName = finalStudentName;
        List<Student> existingStudents = studentRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase(searchName))
                .collect(Collectors.toList());
        
        if (!existingStudents.isEmpty()) {
            log.debug("Estudiante existente encontrado: {}", existingStudents.get(0).getName());
            return existingStudents.get(0);
        }
        
        // 2. Buscar email del estudiante en los inscritos del curso
        String email = null;
        for (StudentCourse sc : enrolledStudents) {
            Student enrolledStudent = sc.getStudent();
            if (enrolledStudent.getName().equalsIgnoreCase(finalStudentName)) {
                email = enrolledStudent.getEmail();
                log.debug("Email encontrado en inscritos del curso: {}", email);
                break;
            }
        }
        
        // 3. Si no se encontró email, generar uno válido
        if (email == null || email.isEmpty()) {
            // Generar email: convertir nombre a email válido
            String baseEmail = finalStudentName.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "") // Eliminar caracteres especiales excepto espacios
                    .trim()
                    .replaceAll("\\s+", "."); // Reemplazar espacios con puntos
            
            // Limitar longitud del email a 255 caracteres (incluyendo @estudiante.com = 15 caracteres)
            int maxLength = 240; // 255 - 15 para "@estudiante.com"
            if (baseEmail.length() > maxLength) {
                baseEmail = baseEmail.substring(0, maxLength);
            }
            
            email = baseEmail + "@estudiante.com";
            
            // Verificar que el email no exista ya
            int suffix = 1;
            String baseEmailForSuffix = baseEmail;
            while (studentRepository.findByEmail(email).isPresent()) {
                String suffixStr = String.valueOf(suffix);
                int maxWithSuffix = maxLength - suffixStr.length() - 1; // -1 para el punto
                if (maxWithSuffix < 0) maxWithSuffix = 0;
                baseEmailForSuffix = baseEmailForSuffix.substring(0, Math.min(baseEmailForSuffix.length(), maxWithSuffix));
                email = baseEmailForSuffix + "." + suffixStr + "@estudiante.com";
                suffix++;
                if (suffix > 1000) { // Prevenir loop infinito
                    email = UUID.randomUUID().toString().substring(0, 8) + "@estudiante.com";
                    break;
                }
            }
            
            log.debug("Email generado: {}", email);
        }
        
        // Crear nuevo estudiante
        Student student = new Student();
        student.setName(finalStudentName);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode("defaultPassword")); // Hasheando la contraseña
        student.setRole(gestor.calificaciones.gestorcalificaciones.enums.Role.STUDENT);
        student.setCode(UUID.randomUUID().toString().substring(0, 8));
        
        log.debug("Creando nuevo estudiante: nombre={}, email={}", finalStudentName, email);
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

package gestor.calificaciones.gestorcalificaciones.enums;

public enum GradeStatus {
    CORRECT("Correcto"),
    INCORRECT("Incorrecto"),
    PENDING("Pendiente"),
    NOT_SUBMITTED("No enviado");
    
    private final String description;
    
    GradeStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

package personal.kcho.backend;

public class StructuredResponse {
    String status;
    String error;
    Object data;

    public StructuredResponse(String status, String error, Object data){
        this.status = status;
        this.error = error;
        this.data = data;
    }
}
package com.mindfulai.Models.kwikapimodels;

public class OperatorCodeResponse {
    private String operator_name;
    private String operator_id;
    private String service_type;
    private String status;
    private String message;

    public String getOperatorName() { return operator_name; }
    public void setOperatorName(String value) { this.operator_name = value; }

    public String getOperatorID() { return operator_id; }
    public void setOperatorID(String value) { this.operator_id = value; }

    public String getServiceType() { return service_type; }
    public void setServiceType(String value) { this.service_type = value; }

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

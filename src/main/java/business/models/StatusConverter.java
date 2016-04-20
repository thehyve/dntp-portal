package business.models;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import business.models.LabRequest.Status;

@Converter(autoApply = true)
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status status) {
        return status.toString();
    }

    @Override
    public Status convertToEntityAttribute(String description) {
        return Status.forDescription(description);
    }

}

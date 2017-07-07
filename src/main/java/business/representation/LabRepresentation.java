package business.representation;

import business.models.Lab;

import javax.validation.constraints.NotNull;

public class LabRepresentation {

    private Long id;

    private Integer number;

    private String name;

    public LabRepresentation() {}

    public LabRepresentation(@NotNull Lab lab) {
        this.setId(lab.getId());
        this.setNumber(lab.getNumber());
        this.setName(lab.getName());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

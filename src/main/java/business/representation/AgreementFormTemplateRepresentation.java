package business.representation;

import business.models.AgreementFormTemplate;

public class AgreementFormTemplateRepresentation {

    private String contents;

    public AgreementFormTemplateRepresentation() { }

    public AgreementFormTemplateRepresentation(
            AgreementFormTemplate template) {
        this.contents = template.getContents();
    }

    public String getContents() {
        return contents;
    }

    public void setContent(String contents) {
        this.contents = contents;
    }

}

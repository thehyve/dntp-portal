/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
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

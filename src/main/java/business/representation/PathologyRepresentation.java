/**
 * Copyright (C) 2016  Stichting PALGA
 * This file is distributed under the GNU Affero General Public License
 * (see accompanying file <a href="{@docRoot}/LICENSE">LICENSE</a>).
 */
package business.representation;

import java.util.ArrayList;
import java.util.List;

import business.models.PathologyItem;

public class PathologyRepresentation {

    private Long id;
    
    String paNumber;
    
    List<String> samples = new ArrayList<String>();

    public PathologyRepresentation() {
        
    }

    public PathologyRepresentation(PathologyItem item) {
        this.id = item.getId();
        this.paNumber = item.getPaNumber();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaNumber() {
        return paNumber;
    }

    public void setPaNumber(String paNumber) {
        this.paNumber = paNumber;
    }

    public List<String> getSamples() {
        return samples;
    }

    public void setSamples(List<String> samples) {
        this.samples = samples;
    }
    
    public void mapSamples(PathologyItem item) {
        this.samples = item.getSamples();
    }
    
}

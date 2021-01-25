package config.webapp.domain;

import java.util.List;

public class MapDomain {
    private String value;
    private String label;
    private List<MapDomain> children;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<MapDomain> getChildren() {
        return children;
    }

    public void setChildren(List<MapDomain> children) {
        this.children = children;
    }
}

package edu.tarleton.drduplex.nicad;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the NiCad-like format.
 *
 * @author Zdenek Tronicek
 */
@XmlType(name = "clone", propOrder = {"nlines", "distance", "sources"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class NiCadClone {

    private Integer nlines;
    private Integer distance;
    private List<NiCadSource> sources;

    public NiCadClone() {
    }

    public NiCadClone(Integer nlines, Integer distance, List<NiCadSource> sources) {
        this.nlines = nlines;
        this.distance = distance;
        this.sources = sources;
    }

    @XmlAttribute
    public Integer getNlines() {
        return nlines;
    }

    public void setNlines(Integer nlines) {
        this.nlines = nlines;
    }

    @XmlAttribute
    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @XmlElement(name = "source")
    public List<NiCadSource> getSources() {
        return sources;
    }

    public void setSources(List<NiCadSource> sources) {
        this.sources = sources;
    }
}

package nextstep.subway.domain;

import nextstep.subway.domain.exception.DuplicateAddSectionException;
import nextstep.subway.domain.exception.IllegalDistanceSectionException;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Section implements Comparable<Section> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "line_id")
    private Line line;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    private int distance;

    protected Section() {
    }

    public Section(Line line, Station upStation, Station downStation, int distance) {
        this.line = line;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public Section makeNext(Line line, Station upStation, Station downStation, int distance) {
        if (this.upStation.equals(upStation) && this.downStation.equals(downStation)) {
            throw new DuplicateAddSectionException();
        }

        return new Section(line, upStation, downStation, distance);
    }

    public Long getId() {
        return id;
    }

    public Line getLine() {
        return line;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public int getDistance() {
        return distance;
    }

    public void changeUpStation(Station requestUpStation, int requestDistance) {
        this.upStation = requestUpStation;
        minusDistance(requestDistance);
    }

    public void changeDownStation(Station requestDownStation, int distance) {
        this.downStation = requestDownStation;
        this.distance = this.distance + distance;
    }

    private void minusDistance(int requestDistance) {
        if (this.distance <= requestDistance) {
            throw new IllegalDistanceSectionException();
        }

        this.distance = this.distance - requestDistance;
    }

    @Override
    public int compareTo(Section o) {
        if (this.getDownStation().equals(o.getUpStation())) {
            return -1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return distance == section.distance && Objects.equals(id, section.id) && Objects.equals(line, section.line) && Objects.equals(upStation, section.upStation) && Objects.equals(downStation, section.downStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, line, upStation, downStation, distance);
    }

}
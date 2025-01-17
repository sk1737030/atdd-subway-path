package nextstep.subway.domain;

import nextstep.subway.domain.exception.IllegalAddSectionException;
import nextstep.subway.domain.exception.IllegalRemoveMinSectionSize;
import nextstep.subway.domain.exception.NotFoundSectionsException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Sections {

    private static final int MINIMUM_SECTION_SIZE = 1;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Sections() {
    }

    public Sections(List<Section> sections) {
        this.sections = sections;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void addSection(Line line, Station requestUpStation, Station requestDownStation, int requestDistance) {
        if (sections.isEmpty()) {
            sections.add(new Section(line, requestUpStation, requestDownStation, requestDistance));
            return;
        }

        Section section = getSectionToAdd(requestUpStation, requestDownStation);

        sections.add(section.makeNext(line, requestUpStation, requestDownStation, requestDistance));

        if (canAddInTheMiddleStation(requestUpStation, section)) {
            section.changeUpStation(requestDownStation, requestDistance);
        }
    }

    public List<Station> getOrderedStations() {
        return sections.stream().sorted()
            .map(section -> List.of(section.getUpStation(), section.getDownStation()))
            .flatMap(Collection::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    public void removeSection(Station station) {
        validateWhenRemoveSection();

        // 마지막 역인지
        removeProcess(station);
    }

    public Section findSectionByStation(Station station) {
        if (isLastStation(station)) {
            return getLastStation();
        }

        return sections.stream().filter(section -> section.getUpStation().equals(station))
            .findFirst()
            .orElseThrow(NotFoundSectionsException::new);
    }

    public boolean isSectionsEmpty() {
        return sections.isEmpty();
    }

    private Section getBeforeSection(Section targetSection) {
        return sections.stream().filter(section -> section.getDownStation().equals(targetSection.getUpStation()))
            .findFirst()
            .orElseThrow(NotFoundSectionsException::new);
    }

    private boolean isFirstSection(Section targetSection) {
        return sections.get(0).equals(targetSection);
    }

    private boolean isLastStation(Station targetStation) {
        return getLastStation().getDownStation().equals(targetStation);
    }

    private Section getLastStation() {
        return sections.get(sections.size() - MINIMUM_SECTION_SIZE);
    }

    private Section getSectionToAdd(Station requestUpStation, Station requestDownStation) {
        return sections.stream().filter(section -> isPossibleToAddSection(requestUpStation, requestDownStation, section))
            .findFirst()
            .orElseThrow(IllegalAddSectionException::new);
    }

    private boolean isPossibleToAddSection(Station requestUpStation, Station requestDownStation, Section section) {
        // 상행역 또는 하행역에 추가
        if (canAddUpOrDownStation(requestUpStation, requestDownStation, section)) {
            return true;
        }

        // 상행역 하행역 사이 추가
        return canAddInTheMiddleStation(requestUpStation, section);
    }

    private boolean canAddUpOrDownStation(Station requestUpStation, Station requestDownStation, Section section) {
        return section.getUpStation().equals(requestDownStation) || section.getDownStation().equals(requestUpStation);
    }

    private boolean canAddInTheMiddleStation(Station requestUpStation, Section section) {
        return section.getUpStation().equals(requestUpStation);
    }

    private void validateWhenRemoveSection() {
        if (sections.size() == MINIMUM_SECTION_SIZE) {
            throw new IllegalRemoveMinSectionSize();
        }
    }

    private void removeProcess(Station station) {
        if (removeLastSection(station)) {
            return;
        }

        // 타겟 구간 찾기
        Section targetSection = findSectionByStation(station);

        if (removeFirstSection(targetSection)) {
            return;
        }

        // 중간 역 제거
        removeMiddleSection(targetSection);
    }

    private void removeMiddleSection(Section targetSection) {
        Section targetBeforeSection = getBeforeSection(targetSection);
        targetBeforeSection.changeDownStation(targetSection.getDownStation(), targetSection.getDistance());
        sections.remove(targetSection);
    }

    private boolean removeFirstSection(Section targetSection) {
        if (isFirstSection(targetSection)) {
            sections.remove(targetSection);
            return true;
        }
        return false;
    }

    private boolean removeLastSection(Station station) {
        if (isLastStation(station)) {
            sections.remove(sections.size() - MINIMUM_SECTION_SIZE);
            return true;
        }
        return false;
    }

}

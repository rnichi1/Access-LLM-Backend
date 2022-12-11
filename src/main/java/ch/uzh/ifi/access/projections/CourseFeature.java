package ch.uzh.ifi.access.projections;

import ch.uzh.ifi.access.model.Course;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDate;

@Projection(types = {Course.class})
public interface CourseFeature {

    Long getId();

    String getUrl();

    String getTitle();

    String getUniversity();

    String getSemester();

    String getDescription();

    String getFeedback();

    String getRestricted();

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getStartDate();

    @JsonFormat(pattern = "dd-MM-yyyy")
    LocalDate getEndDate();

    Long getStudentsCount();
}

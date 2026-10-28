package com.university.website.dto;

import com.university.website.model.enums.FileFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteFilterDto {

    private String query;
    private Long programId;
    private Long subjectId;
    private Long topicId;
    private FileFormat format;
    private Integer minRating;
    private String tag;
    private String sortBy = "recent"; // "recent", "downloads", "rating", "views"
    private int page = 0;
    private int size = 12;
}

package com.bloodbank.hospitalservice.entity;

import com.bloodbank.common.model.BranchScopedEntity;
import com.bloodbank.hospitalservice.enums.FeedbackCategoryEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hospital_feedback")
@FilterDef(name = "branchFilter", parameters = @ParamDef(name = "branchId", type = UUID.class))
@Filter(name = "branchFilter", condition = "branch_id = :branchId")
public class HospitalFeedback extends BranchScopedEntity {

    @Column(name = "hospital_id", nullable = false)
    private UUID hospitalId;

    @Column(name = "request_id")
    private UUID requestId;

    @Column(name = "feedback_date", nullable = false)
    private Instant feedbackDate;

    @Column(name = "rating")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private FeedbackCategoryEnum category;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "responded_by", length = 255)
    private String respondedBy;

    @Column(name = "responded_at")
    private Instant respondedAt;

    public HospitalFeedback() {}

    public UUID getHospitalId() { return hospitalId; }
    public void setHospitalId(UUID hospitalId) { this.hospitalId = hospitalId; }

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }

    public Instant getFeedbackDate() { return feedbackDate; }
    public void setFeedbackDate(Instant feedbackDate) { this.feedbackDate = feedbackDate; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public FeedbackCategoryEnum getCategory() { return category; }
    public void setCategory(FeedbackCategoryEnum category) { this.category = category; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getRespondedBy() { return respondedBy; }
    public void setRespondedBy(String respondedBy) { this.respondedBy = respondedBy; }

    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
}

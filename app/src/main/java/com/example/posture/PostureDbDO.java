package com.example.posture;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "posturedetection-mobilehub-1869887158-posture-db")

public class PostureDbDO {
    private String _userId;
    private Double _exerciseId;
    private String _exerciseName;
    private Boolean _isExerciseOn;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "exercise_id")
    public Double getExerciseId() {
        return _exerciseId;
    }

    public void setExerciseId(final Double _exerciseId) {
        this._exerciseId = _exerciseId;
    }
    @DynamoDBAttribute(attributeName = "exercise_name")
    public String getExerciseName() {
        return _exerciseName;
    }

    public void setExerciseName(final String _exerciseName) {
        this._exerciseName = _exerciseName;
    }
    @DynamoDBAttribute(attributeName = "is_exercise_on")
    public Boolean getIsExerciseOn() {
        return _isExerciseOn;
    }

    public void setIsExerciseOn(final Boolean _isExerciseOn) {
        this._isExerciseOn = _isExerciseOn;
    }

}

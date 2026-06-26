package com.biodataai.backend.dto;

import com.biodataai.backend.entity.BiodataPhoto;
import com.biodataai.backend.entity.PhotoType;
import java.util.UUID;

public record PhotoDto(UUID photoId, PhotoType photoType, String storageUrl, int sortOrder) {

    public static PhotoDto from(BiodataPhoto photo) {
        return new PhotoDto(photo.getId(), photo.getPhotoType(), photo.getStorageUrl(), photo.getSortOrder());
    }
}

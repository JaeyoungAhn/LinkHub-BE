package com.tenten.linkhub.domain.space.service.dto.link;

import java.util.Objects;

public record LinkUpdateRequest(
        Long spaceId,
        String url,
        String title,
        String tagName,
        Long memberId,
        Long linkId,
        String color
) {
    public boolean hasUpdateTagInfo() {
        return Objects.nonNull(tagName) && Objects.nonNull(color);
    }
}

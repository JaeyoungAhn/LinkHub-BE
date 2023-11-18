package com.tenten.linkhub.domain.space.service.dto.space;

import com.tenten.linkhub.domain.space.repository.common.dto.SpaceAndOwnerNickName;
import org.springframework.data.domain.Slice;

import java.util.Objects;

public record PublicSpacesFindByQueryResponses(Slice<SpacesFindByQueryResponse> responses) {

    public static PublicSpacesFindByQueryResponses from(Slice<SpaceAndOwnerNickName> response){
        Slice<SpacesFindByQueryResponse> mapResponses = response.map(s -> new SpacesFindByQueryResponse(
                s.space().getId(),
                s.space().getSpaceName(),
                Objects.isNull(s.space().getDescription()) ? "" : s.space().getDescription(),
                s.space().getCategory(),
                s.space().getIsVisible(),
                s.space().getIsComment(),
                s.space().getIsLinkSummarizable(),
                s.space().getIsReadMarkEnabled(),
                s.space().getViewCount(),
                s.space().getScrapCount(),
                s.space().getFavoriteCount(),
                s.space().getSpaceImages().isEmpty() ? null : s.space().getSpaceImages().get(0).getPath(),
                s.ownerNickName()
        ));

        return new PublicSpacesFindByQueryResponses(mapResponses);
    }

}

package com.tenten.linkhub.domain.space.controller.dto.space;

import com.tenten.linkhub.domain.space.service.dto.space.PublicSpacesFindByQueryResponses;
import com.tenten.linkhub.global.util.PageMetaData;
import org.springframework.data.domain.Slice;

import java.util.List;

public record MySpacesFindApiResponses(
        List<MySpacesFindApiResponse> responses,
        PageMetaData metaData
) {
    public static MySpacesFindApiResponses from(PublicSpacesFindByQueryResponses responses){
        Slice<MySpacesFindApiResponse> mapResponses = responses.responses()
                .map(r -> new MySpacesFindApiResponse(
                        r.spaceId(),
                        r.spaceName(),
                        r.description(),
                        r.category(),
                        r.viewCount(),
                        r.scrapCount(),
                        r.favoriteCount(),
                        r.spaceImagePath(),
                        r.ownerNickName()));

        PageMetaData pageMetaData = new PageMetaData(
                mapResponses.hasNext(),
                mapResponses.getSize(),
                mapResponses.getNumber());

        return new MySpacesFindApiResponses(
                mapResponses.getContent(),
                pageMetaData);
    }

}

package com.tenten.linkhub.domain.space.controller.mapper;

import com.tenten.linkhub.domain.space.controller.dto.link.LinkCreateApiRequest;
import com.tenten.linkhub.domain.space.controller.dto.link.LinkCreateApiResponse;
import com.tenten.linkhub.domain.space.controller.dto.link.LinkGetWithFilterApiRequests;
import com.tenten.linkhub.domain.space.controller.dto.link.LinkGetWithFilterApiResponses;
import com.tenten.linkhub.domain.space.controller.dto.link.LinkUpdateApiRequest;
import com.tenten.linkhub.domain.space.controller.dto.link.LinkUpdateApiResponse;
import com.tenten.linkhub.domain.space.facade.dto.LinkCreateFacadeRequest;
import com.tenten.linkhub.domain.space.facade.dto.LinkGetByQueryResponses;
import com.tenten.linkhub.domain.space.facade.dto.LinkUpdateFacadeRequest;
import com.tenten.linkhub.domain.space.model.link.Color;
import com.tenten.linkhub.domain.space.service.dto.link.LinksGetByQueryRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.PageRequest;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface LinkApiMapper {

    @Mapping(target = "color", source = "color", qualifiedByName = "toColor")
    LinkCreateFacadeRequest toLinkCreateFacadeRequest(LinkCreateApiRequest apiRequest);

    LinkCreateApiResponse toLinkCreateApiResponse(Long linkId);

    @Mapping(source = "updateLinkId", target = "linkId")
    LinkUpdateApiResponse toLinkUpdateApiResponse(Long updateLinkId);

    LinkUpdateFacadeRequest toLinkUpdateFacadeRequest(LinkUpdateApiRequest apiRequest);

    LinkGetWithFilterApiResponses toLinksGetWithFilterApiResponse(LinkGetByQueryResponses facadeResponses);

    LinksGetByQueryRequest toLinksGetByQueryRequest(LinkGetWithFilterApiRequests request, PageRequest pageRequest);
}

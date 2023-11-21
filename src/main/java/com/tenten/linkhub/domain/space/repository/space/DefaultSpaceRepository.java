package com.tenten.linkhub.domain.space.repository.space;

import com.tenten.linkhub.domain.space.model.space.Space;
import com.tenten.linkhub.domain.space.repository.common.dto.SpaceAndOwnerNickName;
import com.tenten.linkhub.domain.space.repository.common.dto.SpaceAndSpaceImageOwnerNickName;
import com.tenten.linkhub.domain.space.repository.space.dto.MySpacesQueryCondition;
import com.tenten.linkhub.domain.space.repository.space.dto.QueryCondition;
import com.tenten.linkhub.domain.space.repository.space.query.SpaceQueryRepository;
import com.tenten.linkhub.global.exception.DataNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DefaultSpaceRepository implements SpaceRepository {

    private final SpaceJpaRepository spaceJpaRepository;
    private final SpaceQueryRepository spaceQueryRepository;

    public DefaultSpaceRepository(SpaceJpaRepository spaceJpaRepository, SpaceQueryRepository spaceQueryRepository) {
        this.spaceJpaRepository = spaceJpaRepository;
        this.spaceQueryRepository = spaceQueryRepository;
    }

    @Override
    public Slice<SpaceAndSpaceImageOwnerNickName> findPublicSpacesJoinSpaceImageByQuery(QueryCondition queryCondition) {
        return spaceQueryRepository.findPublicSpacesJoinSpaceImageByCondition(queryCondition);
    }

    @Override
    public Space save(Space space) {
        return spaceJpaRepository.save(space);
    }

    @Override
    public Space getById(Long spaceId) {
        return spaceJpaRepository.findById(spaceId)
                .orElseThrow(() -> new DataNotFoundException("해당 spaceId를 가진 Space를 찾을 수 없습니다."));
    }

    @Override
    public Space getSpaceJoinSpaceMemberById(Long spaceId) {
        return spaceJpaRepository.findSpaceJoinSpaceMemberById(spaceId)
                .orElseThrow(() -> new DataNotFoundException("해당 spaceId를 가진 SpaceWithSpaceImage를 찾을 수 없습니다."));
    }

    @Override
    public Slice<SpaceAndSpaceImageOwnerNickName> findMySpacesJoinSpaceImageByQuery(MySpacesQueryCondition queryCondition) {
        return spaceQueryRepository.findMySpacesJoinSpaceImageByCondition(queryCondition);
    }

    @Override
    @Transactional
    public void increaseFavoriteCount(Long spaceId) {
        spaceJpaRepository.increaseFavoriteCount(spaceId);
    }

    @Override
    @Transactional
    public void decreaseFavoriteCount(Long spaceId) {
        spaceJpaRepository.decreaseFavoriteCount(spaceId);
    }

}

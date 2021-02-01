package es.microservices.tests.phones.services.impl;

import org.springframework.util.Assert;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;
import es.microservices.tests.phones.repositories.PhoneRepository;
import es.microservices.tests.phones.services.PhoneService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PhoneServiceImpl implements PhoneService {

  private final PhoneRepository repository;
  private final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper;
  private final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper;

  public PhoneServiceImpl(final PhoneRepository repository,
      final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper,
      final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper) {
    Assert.notNull(repository, "'repository' must not be null");
    Assert.notNull(dto2EntityMapper, "'dto2EntityMapper' must not be null");
    Assert.notNull(entity2DtoMapper, "'entity2DtoMapper' must not be null");
    this.repository = repository;
    this.dto2EntityMapper = dto2EntityMapper;
    this.entity2DtoMapper = entity2DtoMapper;

  }

  @Override
  public Flux<PhoneDto> findAllPhones() {    
    return Flux.defer(this.repository::findAll).map(this.entity2DtoMapper::map);        
  }
  
  @Override
  public Mono<Long> countAllPhones() {
    return Mono.defer(this.repository::count);    
  }
  
  @Override
  public Mono<PhoneDto> findById(String phoneId) {
    Assert.hasText(phoneId, "'phoneId' must not be null or empty");
    return Mono.defer(() -> this.repository.findById(phoneId))
        .map(this.entity2DtoMapper::map);    
  }
  
  @Override
  public Mono<PhoneDto> createPhone(final NewPhoneDto phone) {
    Assert.notNull(phone, "'phone' must not be null");
    return this.dto2EntityMapper.map(Mono.just(phone))
        .flatMap(this::savePhoneEntity)
      ;    
  }
  
  private Mono<PhoneDto> savePhoneEntity(PhoneEntity phone) {
    return Mono.defer(() -> this.repository.save(phone))
        .map(this.entity2DtoMapper::map);
  }

}

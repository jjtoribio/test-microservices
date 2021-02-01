package es.microservices.tests.phones.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.microservices.tests.phones.dtos.NewPhoneDto;
import es.microservices.tests.phones.dtos.PhoneDto;
import es.microservices.tests.phones.entities.PhoneEntity;
import es.microservices.tests.phones.mappers.Mapper;
import es.microservices.tests.phones.mappers.impl.NewPhoneDto2PhoneEntityMapper;
import es.microservices.tests.phones.mappers.impl.PhoneEntity2PhoneDtoMapper;
import es.microservices.tests.phones.repositories.PhoneRepository;
import es.microservices.tests.phones.services.PhoneService;
import es.microservices.tests.phones.services.impl.PhoneServiceImpl;

@Configuration
public class GlobalConfig {

  @Bean
  public Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper() {
    return new NewPhoneDto2PhoneEntityMapper();
  }

  @Bean
  public Mapper<PhoneEntity, PhoneDto> entity2DtoMapper() {
    return new PhoneEntity2PhoneDtoMapper();
  }

  @Bean
  public PhoneService phoneService(final PhoneRepository repository,
      final Mapper<NewPhoneDto, PhoneEntity> dto2EntityMapper,
      final Mapper<PhoneEntity, PhoneDto> entity2DtoMapper) {
    return new PhoneServiceImpl(repository, dto2EntityMapper, entity2DtoMapper);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }


}

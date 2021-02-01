package es.microservices.tests.orders.clients;

import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;

@FunctionalInterface
public interface PhoneClient {

  DaasPhoneCatalogDto getPhoneData(final Integer page, final Integer pageSize);
}

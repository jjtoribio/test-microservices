package es.microservices.tests.orders.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;
import es.microservices.tests.orders.clients.OrderClient;
import es.microservices.tests.orders.clients.PhoneClient;
import es.microservices.tests.orders.configurations.utils.TransformOrdersUtils;
import es.microservices.tests.orders.dtos.NewOrderDto;
import es.microservices.tests.orders.dtos.OrderDto;
import es.microservices.tests.orders.dtos.orders.DaasNewOrderDto;
import es.microservices.tests.orders.dtos.orders.DaasOrderDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneCatalogDto;
import es.microservices.tests.orders.dtos.phones.DaasPhoneDto;
import es.microservices.tests.orders.exceptions.CreatingOrderException;
import es.microservices.tests.orders.exceptions.PhoneRequestedListEmptyException;
import es.microservices.tests.orders.exceptions.PhoneRequestedNotFound;
import es.microservices.tests.orders.exceptions.RetrievingPhoneException;
import es.microservices.tests.orders.services.OrderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderServiceImpl implements OrderService {

  private static final Integer DEFAULT_PAGE_SIZE = Integer.valueOf(500);
  private static final Integer DEFAULT_PAGE = Integer.valueOf(1);

  private final PhoneClient phoneClient;
  private final OrderClient orderClient;

  public OrderServiceImpl(final PhoneClient phoneClient, final OrderClient orderClient) {
    Assert.notNull(phoneClient, "'phoneClient' must be not null");
    Assert.notNull(orderClient, "'orderClient' must be not null");

    this.phoneClient = phoneClient;
    this.orderClient = orderClient;
  }

  @Override
  public OrderDto createOrder(NewOrderDto newOrder) {
    Assert.notNull(newOrder, "'newOrder' must be not null");
    final List<String> phoneIds = newOrder.getPhoneIdListToBuy();

    if (CollectionUtils.isEmpty(phoneIds)) {
      final String errorMessage = "Error. The list of phone identifiers is empty";
      log.error(errorMessage);
      throw new PhoneRequestedListEmptyException(errorMessage);
    }

    final List<DaasPhoneDto> phonesToBuy = retrievePhonesToBuyData(phoneIds);
    try {
      final DaasNewOrderDto daasNewOrderDto =
          TransformOrdersUtils.transformNewOrderDto(newOrder, phonesToBuy);
      final DaasOrderDto daasOrderDto = orderClient.createOrder(daasNewOrderDto);
      return TransformOrdersUtils.transformOrderDto(daasOrderDto);
    } catch (HttpStatusCodeException e) {
      log.error(e.getMessage(), e);
      throw new CreatingOrderException("An error occurred creating the order", e);
    }
  }

  private List<DaasPhoneDto> retrievePhonesToBuyData(List<String> requestedPhoneIds) {
    final List<DaasPhoneDto> catalog = getAllPhones();
    final List<DaasPhoneDto> phonesToBuy =
        catalog.stream().filter(phone -> requestedPhoneIds.contains(phone.getPhoneId()))
            .collect(Collectors.toList());

    if (phonesToBuy.size() != requestedPhoneIds.size()) {
      final List<String> phonesToBuyIds = extractPhoneIds(phonesToBuy);
      // @formatter:off
      final List<String> phonesIdsNotFound = requestedPhoneIds.stream()
          .filter(id -> !phonesToBuyIds.contains(id))
          .collect(Collectors.toList());
      // @formatter:on
      final String errorMessage = createErrorMessage(phonesIdsNotFound);
      log.error(errorMessage);
      throw new PhoneRequestedNotFound(errorMessage);
    }
    return phonesToBuy;
  }


  private static String createErrorMessage(final List<String> phoneIdsNotFound) {
    final StringBuilder sb = new StringBuilder(
        "Cannot continue with order creation. The following phone identifiers have not been found: ");
    phoneIdsNotFound.stream().forEach(id -> sb.append(id).append(", "));
    return StringUtils.removeEnd(sb.toString(), ", ");
  }

  private static List<String> extractPhoneIds(final List<DaasPhoneDto> phonesToBuy) {
    return phonesToBuy.stream().map(DaasPhoneDto::getPhoneId).collect(Collectors.toList());
  }

  private List<DaasPhoneDto> getAllPhones() {
    final List<DaasPhoneDto> phonesCatalog = new ArrayList<>();
    int page = DEFAULT_PAGE.intValue();
    long totalCount = 0;

    try {
      do {
        final DaasPhoneCatalogDto catalog =
            this.phoneClient.getPhoneData(page++, DEFAULT_PAGE_SIZE);
        if (Objects.nonNull(catalog.getPhones()) && !catalog.getPhones().isEmpty()) {
          phonesCatalog.addAll(catalog.getPhones());
        }
        totalCount = catalog.getTotalCount();
      } while (totalCount > phonesCatalog.size());
    } catch (HttpStatusCodeException e) {
      final String errorMessage = "An error occurred while retrieving the phone catalog";
      log.error(errorMessage, e);

      if (phonesCatalog.isEmpty()) {
        throw new RetrievingPhoneException("An error occurred while retrieving the phone catalog",
            e);
      }
    }
    return phonesCatalog;
  }


}

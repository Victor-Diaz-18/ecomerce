package edu.unimagdalena.universitystore.config;

import edu.unimagdalena.universitystore.entity.Address;
import edu.unimagdalena.universitystore.entity.PurchaseOrder;
import edu.unimagdalena.universitystore.repository.AddressRepository;
import edu.unimagdalena.universitystore.repository.PurchaseOrderRepository;
import edu.unimagdalena.universitystore.service.ColombiaLocations;
import edu.unimagdalena.universitystore.service.StreetFormat;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Completa las direcciones creadas antes del formulario por partes: escribe la ciudad con su nombre
 * oficial, le pone el departamento y deja la calle con el formato "Calle 13 # 3-84".
 * El barrio no se puede deducir, así que queda vacío hasta que alguien edite la dirección.
 * Solo toca las que aún no tienen departamento, por lo que repetirlo en cada arranque no cambia nada.
 * Después fija la dirección de entrega de las órdenes que ya habían salido de la tienda.
 */
@Component
@RequiredArgsConstructor
public class AddressBackfill implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AddressBackfill.class);

    private final AddressRepository addressRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Address> incomplete = addressRepository.findWithoutDepartment();
        int completed = 0;

        for (Address address : incomplete) {
            var city = ColombiaLocations.find(address.getCity());
            if (city.isEmpty()) {
                log.warn("Dirección {} sin completar: la ciudad '{}' no está en la lista de destinos",
                        address.getId(), address.getCity());
                continue;
            }
            address.setCity(city.get().name());
            address.setDepartment(city.get().department());
            address.setCountry(ColombiaLocations.COUNTRY);
            address.setStreet(StreetFormat.normalize(address.getStreet()));
            completed++;
        }

        if (completed > 0) {
            log.info("Direcciones antiguas completadas con ciudad oficial y departamento: {}", completed);
        }

        // Orders shipped before address copies existed: freeze the address they have today
        List<PurchaseOrder> shipped = purchaseOrderRepository.findShippedWithoutAddressCopy();
        shipped.forEach(order -> order.setShippingAddress(order.getAddress().toLine()));
        if (!shipped.isEmpty()) {
            log.info("Órdenes ya enviadas a las que se les fijó la dirección de entrega: {}", shipped.size());
        }
    }
}

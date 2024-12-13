package vn.edu.iuh.fit.week05.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.week05.backend.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findAddressById(Long id);
}

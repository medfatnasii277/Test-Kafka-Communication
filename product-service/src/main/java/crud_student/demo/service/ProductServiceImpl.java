package crud_student.demo.service;

import crud_student.demo.dto.ProductRequestDTO;
import crud_student.demo.dto.ProductResponseDTO;
import crud_student.demo.entity.Product;
import crud_student.demo.exception.NotFoundException;
import crud_student.demo.mapper.ProductMapper;
import crud_student.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.example.product.protobuf.ProductEventProto;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    @Autowired
    private ProductEventPublisher eventPublisher;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        Product product = ProductMapper.toEntity(dto);
        Product saved = productRepository.save(product);
        // Publish event
        ProductEventProto.Product protoProduct = ProductEventProto.Product.newBuilder()
            .setId(saved.getId().toString())
            .setName(saved.getName())
            .setDescription(saved.getDescription() == null ? "" : saved.getDescription())
            .setPrice(saved.getPrice().doubleValue())
            .setStock(0) // No stock in product-service
            .build();
        ProductEventProto.ProductEvent event = ProductEventProto.ProductEvent.newBuilder()
            .setEventType(ProductEventProto.ProductEvent.EventType.PRODUCT_CREATED)
            .setProduct(protoProduct)
            .build();
        eventPublisher.publishProductEvent(event);
        return ProductMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        return ProductMapper.toResponseDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        ProductMapper.updateEntity(product, dto);
        Product updated = productRepository.save(product);
        // Publish event
        ProductEventProto.Product protoProduct = ProductEventProto.Product.newBuilder()
            .setId(updated.getId().toString())
            .setName(updated.getName())
            .setDescription(updated.getDescription() == null ? "" : updated.getDescription())
            .setPrice(updated.getPrice().doubleValue())
            .setStock(0)
            .build();
        ProductEventProto.ProductEvent event = ProductEventProto.ProductEvent.newBuilder()
            .setEventType(ProductEventProto.ProductEvent.EventType.PRODUCT_UPDATED)
            .setProduct(protoProduct)
            .build();
        eventPublisher.publishProductEvent(event);
        return ProductMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found with id: " + id);
        }
        // Publish event before deletion
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            ProductEventProto.Product protoProduct = ProductEventProto.Product.newBuilder()
                .setId(product.getId().toString())
                .setName(product.getName())
                .setDescription(product.getDescription() == null ? "" : product.getDescription())
                .setPrice(product.getPrice().doubleValue())
                .setStock(0)
                .build();
            ProductEventProto.ProductEvent event = ProductEventProto.ProductEvent.newBuilder()
                .setEventType(ProductEventProto.ProductEvent.EventType.PRODUCT_DELETED)
                .setProduct(protoProduct)
                .build();
            eventPublisher.publishProductEvent(event);
        }
        productRepository.deleteById(id);
    }
} 
package crud_student.demo.mapper;

import crud_student.demo.entity.Product;
import crud_student.demo.dto.ProductRequestDTO;
import crud_student.demo.dto.ProductResponseDTO;

public class ProductMapper {
    public static Product toEntity(ProductRequestDTO dto) {
        if (dto == null) return null;
        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .build();
    }

    public static ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) return null;
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public static void updateEntity(Product product, ProductRequestDTO dto) {
        if (dto.getName() != null) product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
    }
} 
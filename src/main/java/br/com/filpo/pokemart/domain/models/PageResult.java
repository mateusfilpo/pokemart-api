package br.com.filpo.pokemart.domain.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> data;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private boolean hasNext;
}
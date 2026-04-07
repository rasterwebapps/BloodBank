package com.bloodbank.reportingservice.service;

import com.bloodbank.common.exceptions.ResourceNotFoundException;
import com.bloodbank.reportingservice.dto.DashboardWidgetCreateRequest;
import com.bloodbank.reportingservice.dto.DashboardWidgetResponse;
import com.bloodbank.reportingservice.entity.DashboardWidget;
import com.bloodbank.reportingservice.enums.WidgetTypeEnum;
import com.bloodbank.reportingservice.mapper.DashboardWidgetMapper;
import com.bloodbank.reportingservice.repository.DashboardWidgetRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardWidgetRepository dashboardWidgetRepository;

    @Mock
    private DashboardWidgetMapper dashboardWidgetMapper;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID widgetId;
    private DashboardWidget widget;
    private DashboardWidgetResponse widgetResponse;

    @BeforeEach
    void setUp() {
        widgetId = UUID.randomUUID();

        widget = new DashboardWidget("WDG-STOCK", "Stock Overview", WidgetTypeEnum.CHART);
        widget.setId(widgetId);
        widget.setDataSource("inventory-service");
        widget.setRefreshIntervalSec(300);
        widget.setSortOrder(1);

        widgetResponse = new DashboardWidgetResponse(
                widgetId, null, "WDG-STOCK", "Stock Overview",
                WidgetTypeEnum.CHART, "inventory-service", null, null,
                300, null, 1, true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create widget successfully")
        void shouldCreate() {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Stock Overview", WidgetTypeEnum.CHART,
                    "inventory-service", null, null, 300, null, 1);

            when(dashboardWidgetMapper.toEntity(request)).thenReturn(widget);
            when(dashboardWidgetRepository.save(any(DashboardWidget.class))).thenReturn(widget);
            when(dashboardWidgetMapper.toResponse(widget)).thenReturn(widgetResponse);

            DashboardWidgetResponse result = dashboardService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.widgetCode()).isEqualTo("WDG-STOCK");
            verify(dashboardWidgetRepository).save(any(DashboardWidget.class));
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return widget when found")
        void shouldReturn() {
            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.of(widget));
            when(dashboardWidgetMapper.toResponse(widget)).thenReturn(widgetResponse);

            DashboardWidgetResponse result = dashboardService.getById(widgetId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(widgetId);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.getById(widgetId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getByCode")
    class GetByCode {

        @Test
        @DisplayName("should return widget by code")
        void shouldReturnByCode() {
            when(dashboardWidgetRepository.findByWidgetCode("WDG-STOCK")).thenReturn(Optional.of(widget));
            when(dashboardWidgetMapper.toResponse(widget)).thenReturn(widgetResponse);

            DashboardWidgetResponse result = dashboardService.getByCode("WDG-STOCK");

            assertThat(result.widgetCode()).isEqualTo("WDG-STOCK");
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(dashboardWidgetRepository.findByWidgetCode("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.getByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getActiveWidgets")
    class GetActiveWidgets {

        @Test
        @DisplayName("should return all active widgets")
        void shouldReturnActive() {
            when(dashboardWidgetRepository.findByActiveTrueOrderBySortOrderAsc())
                    .thenReturn(List.of(widget));
            when(dashboardWidgetMapper.toResponseList(List.of(widget)))
                    .thenReturn(List.of(widgetResponse));

            List<DashboardWidgetResponse> result = dashboardService.getActiveWidgets();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update widget successfully")
        void shouldUpdate() {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Updated Widget", WidgetTypeEnum.TABLE,
                    "inventory-service", null, null, 600, null, 2);

            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.of(widget));
            when(dashboardWidgetRepository.save(any(DashboardWidget.class))).thenReturn(widget);
            when(dashboardWidgetMapper.toResponse(widget)).thenReturn(widgetResponse);

            DashboardWidgetResponse result = dashboardService.update(widgetId, request);

            assertThat(result).isNotNull();
            verify(dashboardWidgetMapper).updateEntity(request, widget);
            verify(dashboardWidgetRepository).save(widget);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            DashboardWidgetCreateRequest request = new DashboardWidgetCreateRequest(
                    null, "WDG-STOCK", "Updated Widget", WidgetTypeEnum.TABLE,
                    "inventory-service", null, null, 600, null, 2);

            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.update(widgetId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should soft delete widget")
        void shouldDelete() {
            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.of(widget));
            when(dashboardWidgetRepository.save(any(DashboardWidget.class))).thenReturn(widget);

            dashboardService.delete(widgetId);

            assertThat(widget.isActive()).isFalse();
            verify(dashboardWidgetRepository).save(widget);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrow() {
            when(dashboardWidgetRepository.findById(widgetId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dashboardService.delete(widgetId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}

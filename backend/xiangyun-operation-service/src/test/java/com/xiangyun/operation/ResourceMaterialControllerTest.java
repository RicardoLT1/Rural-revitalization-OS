package com.xiangyun.operation;

import com.xiangyun.common.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ResourceMaterialControllerTest {

    @Test
    void userCannotReadManagedMaterials() {
        ResourceMaterialController controller = new ResourceMaterialController(mock(ResourceMaterialService.class));

        assertThatThrownBy(() -> controller.list("101", "USER"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("工作人员或管理员");
    }

    @Test
    void staffCannotDeleteManagedMaterials() {
        ResourceMaterialController controller = new ResourceMaterialController(mock(ResourceMaterialService.class));

        assertThatThrownBy(() -> controller.delete("101", "1", "STAFF", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("仅管理员");
    }
}

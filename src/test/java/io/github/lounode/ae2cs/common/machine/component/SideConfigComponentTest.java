package io.github.lounode.ae2cs.common.machine.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证侧面自动物流策略，确保双向面不会产生自动回流。
 */
class SideConfigComponentTest {

    /**
     * 双向面在同时开启两种自动模式时必须只输出；专用输入面仍可正常自动输入。
     */
    @Test
    void preventsBidirectionalAutoTransfer() {
        assertFalse(SideConfigComponent.shouldAutoImport(SidePolicy.ALL, true, true));
        assertTrue(SideConfigComponent.shouldAutoImport(SidePolicy.INSERT, true, true));
        assertTrue(SideConfigComponent.shouldAutoImport(SidePolicy.ALL, true, false));
        assertFalse(SideConfigComponent.shouldAutoImport(SidePolicy.EXTRACT, true, true));
        assertFalse(SideConfigComponent.shouldAutoImport(SidePolicy.INSERT, false, true));
    }
}

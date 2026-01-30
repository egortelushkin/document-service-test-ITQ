import com.helloegor03.Main;
import com.helloegor03.api.CreateDocumentRequest;
import com.helloegor03.domain.Document;
import com.helloegor03.domain.DocumentAction;
import com.helloegor03.domain.DocumentStatus;
import com.helloegor03.repository.ApprovalRegistryRepository;
import com.helloegor03.repository.DocumentHistoryRepository;
import com.helloegor03.repository.DocumentRepository;
import com.helloegor03.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import com.helloegor03.domain.DocumentHistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentServiceRollbackTest {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentHistoryRepository historyRepository;

    @MockBean
    private ApprovalRegistryRepository registryRepository; // только здесь мок

    @Test
    void approve_shouldRollback_ifRegistryFails() {
        Long id = documentService.create(
                new CreateDocumentRequest("Ivan", "Rollback test")
        );
        documentService.submit(id, "Ivan");

        // simmulate DB error during saving to registry
        doThrow(new RuntimeException("DB error"))
                .when(registryRepository)
                .save(any());

        assertThatThrownBy(() -> documentService.approve(id, "Manager"))
                .isInstanceOf(RuntimeException.class);

        Document doc = documentRepository.findById(id).orElseThrow();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);

        assertThat(historyRepository.findAll())
                .extracting(DocumentHistory::getAction)
                .doesNotContain(DocumentAction.APPROVE);
    }

}

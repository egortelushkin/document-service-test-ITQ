import com.helloegor03.Main;
import com.helloegor03.api.BatchResultDto;
import com.helloegor03.api.CreateDocumentRequest;
import com.helloegor03.api.OperationResult;
import com.helloegor03.domain.*;
import com.helloegor03.repository.ApprovalRegistryRepository;
import com.helloegor03.repository.DocumentRepository;
import com.helloegor03.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApprovalRegistryRepository registryRepository;

    @BeforeEach
    void cleanDb() {
        registryRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    @DirtiesContext
    void approve_shouldApproveDocument_andCreateRegistryRecord() {
        // given
        Long id = documentService.create(
                new CreateDocumentRequest("Ivan", "Test doc")
        );
        documentService.submit(id, "Ivan");

        // when
        OperationResult result = documentService.approve(id, "Manager");

        // then
        assertThat(result).isEqualTo(OperationResult.SUCCESS);

        Document doc = documentRepository.findById(id).orElseThrow();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.APPROVED);

        assertThat(registryRepository.findAll())
                .hasSize(1)
                .extracting(ApprovalRegistry::getDocumentId)
                .containsExactly(id);
    }


    @Test
    void batchApprove_shouldReturnPartialResults() {
        // given
        Long doc1 = documentService.create(
                new CreateDocumentRequest("A", "Doc 1")
        );
        Long doc2 = documentService.create(
                new CreateDocumentRequest("B", "Doc 2")
        );
        Long doc3 = 999L; // is not exist

        documentService.submit(doc1, "A");

        // when
        List<BatchResultDto> results =
                documentService.approveBatch(
                        List.of(doc1, doc2, doc3),
                        "Manager"
                );

        // then
        assertThat(results)
                .extracting(BatchResultDto::result)
                .containsExactlyInAnyOrder(
                        OperationResult.SUCCESS,
                        OperationResult.CONFLICT,
                        OperationResult.NOT_FOUND
                );
    }
}

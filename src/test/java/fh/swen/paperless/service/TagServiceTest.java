package fh.swen.paperless.service;

import fh.swen.paperless.domain.Document;
import fh.swen.paperless.domain.Tag;
import fh.swen.paperless.exception.NotFoundException;
import fh.swen.paperless.exception.TagNameTakenException;
import fh.swen.paperless.persistence.DocumentRepository;
import fh.swen.paperless.persistence.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;           // Attrappe statt echter Datenbank

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private TagService tagService;                 // echter Service, bekommt die Mocks injiziert

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = Tag.builder().id(1L).name("Rechnung").build();
    }

    @Test
    void fetchAll_returnsAllTags() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        assertThat(tagService.fetchAll()).containsExactly(tag);
    }

    @Test
    void fetchById_returnsTag_whenFound() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        assertThat(tagService.fetchById(1L)).isEqualTo(tag);
    }

    @Test
    void fetchById_throwsNotFound_whenMissing() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.fetchById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesTagWithName_whenNameIsFree() {
        when(tagRepository.existsByNameIgnoreCase("Rechnung")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        Tag created = tagService.create("Rechnung");

        // prüfen, was tatsächlich an das Repository übergeben wurde
        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Rechnung");
        assertThat(created).isEqualTo(tag);
    }

    @Test
    void create_throwsTagNameTaken_whenNameExists() {
        when(tagRepository.existsByNameIgnoreCase("rechnung")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create("rechnung"))
                .isInstanceOf(TagNameTakenException.class)
                .hasMessageContaining("rechnung");

        verify(tagRepository, never()).save(any());
    }

    @Test
    void delete_removesTagFromAllDocuments_thenDeletesTag() {
        Tag other = Tag.builder().id(2L).name("Vertrag").build();
        Document a = Document.builder().id(10L).tags(new HashSet<>(Set.of(tag, other))).build();
        Document b = Document.builder().id(11L).tags(new HashSet<>(Set.of(tag))).build();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(documentRepository.findAllByTagsId(1L)).thenReturn(List.of(a, b));

        tagService.delete(1L);

        // Tag wird aus der Join-Tabelle entfernt, andere Tags bleiben erhalten
        assertThat(a.getTags()).containsExactly(other);
        assertThat(b.getTags()).isEmpty();
        verify(tagRepository).delete(tag);
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(tagRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.delete(99L))
                .isInstanceOf(NotFoundException.class);

        verify(tagRepository, never()).delete(any());
        verifyNoInteractions(documentRepository);
    }
}

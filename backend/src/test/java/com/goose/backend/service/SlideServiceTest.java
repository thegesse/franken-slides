package com.goose.backend.service;

import com.goose.backend.dto.SlideResponse;
import com.goose.backend.mapper.SlideContentJsonConverter;
import com.goose.backend.model.Slide;
import com.goose.backend.model.SlideGroup;
import com.goose.backend.repository.SlideGroupRepository;
import com.goose.backend.repository.SlideRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlideServiceTest {
    @Mock SlideRepository slideRepository;
    @Mock SlideGroupRepository slideGroupRepository;
    @Mock SlideContentJsonConverter contentJsonConverter;
    @InjectMocks SlideService slideService;

    @Test
    void createSlideUsesDefaultsForBlankFieldsAndSerializesContent() {
        SlideGroup group = group(30L);
        JsonNode content = mock(JsonNode.class);
        when(slideGroupRepository.findByIdAndTheme_IdAndTheme_Owner_Id(30L, 7L, 3L))
                .thenReturn(Optional.of(group));
        when(contentJsonConverter.serialize(content)).thenReturn("{\"text\":\"hello\"}");
        when(contentJsonConverter.parse("{\"text\":\"hello\"}")).thenReturn(content);
        when(slideRepository.save(any(Slide.class))).thenAnswer(invocation -> {
            Slide slide = invocation.getArgument(0);
            slide.setId(40L);
            return slide;
        });

        SlideResponse response = slideService.createSlide(3L, 7L, 30L, " ", null, content);

        assertEquals(40L, response.id());
        assertEquals("empty", response.title());
        assertEquals("empty", response.description());
        assertSame(content, response.content());
        assertEquals(30L, response.groupId());
        verify(slideRepository).save(argThat(slide ->
                "empty".equals(slide.getTitle()) && "empty".equals(slide.getDescription())
                        && "{\"text\":\"hello\"}".equals(slide.getContent())
                        && slide.getGroup() == group));
    }

    @Test
    void getSlideReturnsSlideFromOwnedGroup() {
        SlideGroup group = group(30L);
        Slide slide = slide(40L, group);
        JsonNode content = mock(JsonNode.class);
        when(slideGroupRepository.findByIdAndTheme_IdAndTheme_Owner_Id(30L, 7L, 3L))
                .thenReturn(Optional.of(group));
        when(slideRepository.findById(40L)).thenReturn(Optional.of(slide));
        when(contentJsonConverter.parse(null)).thenReturn(content);

        SlideResponse response = slideService.getSlideById(3L, 7L, 30L, 40L);

        assertEquals(40L, response.id());
        assertSame(content, response.content());
    }

    @Test
    void getSlideRejectsMissingOrUnownedGroup() {
        when(slideGroupRepository.findByIdAndTheme_IdAndTheme_Owner_Id(30L, 7L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> slideService.getSlideById(3L, 7L, 30L, 40L));
        verifyNoInteractions(slideRepository);
    }

    @Test
    void changeSlideTitlePersistsNewTitle() {
        SlideGroup group = group(30L);
        Slide slide = slide(40L, group);
        when(slideGroupRepository.findByIdAndTheme_IdAndTheme_Owner_Id(30L, 7L, 3L))
                .thenReturn(Optional.of(group));
        when(slideRepository.findById(40L)).thenReturn(Optional.of(slide));
        when(slideRepository.save(slide)).thenReturn(slide);

        SlideResponse response = slideService.changeSlideTitle(3L, 7L, 30L, 40L, "New title");

        assertEquals("New title", response.title());
        verify(slideRepository).save(slide);
    }

    private SlideGroup group(Long id) {
        SlideGroup group = new SlideGroup();
        group.setId(id);
        return group;
    }

    private Slide slide(Long id, SlideGroup group) {
        Slide slide = new Slide();
        slide.setId(id);
        slide.setTitle("Title");
        slide.setDescription("Description");
        slide.setGroup(group);
        return slide;
    }
}

package com.ozzz7y.opencalendar.event

import com.ozzz7y.opencalendar.TestConstants.PAGEABLE_PAGE_NUMBER
import com.ozzz7y.opencalendar.TestConstants.PAGEABLE_PAGE_SIZE
import com.ozzz7y.opencalendar.domain.event.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class EventControllerTest {

    @Mock
    private lateinit var _service: EventService

    @InjectMocks
    private lateinit var _controller: EventController

    private lateinit var _dto: EventDto

    private lateinit var _pageable: Pageable

    private val _now: LocalDateTime = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        _dto = EventDto(
            id = UUID.randomUUID(),
            name = "Test",
            description = "Test",
            startDate = _now,
            endDate = _now.plusHours(1),
            recurringPattern = RecurringPattern.NONE,
            calendarId = UUID.randomUUID(),
            categoryId = UUID.randomUUID()
        )
        _pageable = PageRequest.of(
            PAGEABLE_PAGE_NUMBER,
            PAGEABLE_PAGE_SIZE
        )
    }

    @Test
    fun `should return created event with status code 201 Created`() {
        whenever(_service.create(_dto)).thenReturn(_dto)

        val response: ResponseEntity<EventDto> = _controller.create(dto = _dto)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(_dto, response.body)

        verify(_service).create(eq(_dto))
    }

    @Test
    fun `should return paginated list of all events with status code 200 OK`() {
        val dtos: List<EventDto> = listOf(_dto, _dto.copy(), _dto.copy())
        whenever(_service.getAll()).thenReturn(dtos)

        val response: ResponseEntity<Page<EventDto>> = _controller.getAll(pageable = _pageable)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(dtos.size.toLong(), response.body?.totalElements)
        assertEquals(dtos, response.body?.content)

        verify(_service).getAll()
    }

    @Test
    fun `should return event by id with status code 200 OK`() {
        val id: UUID = _dto.id!!
        whenever(_service.getById(id = id)).thenReturn(_dto)

        val response: ResponseEntity<EventDto> = _controller.getById(id = id)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(_dto, response.body)

        verify(_service).getById(id = id)
    }

    @Test
    fun `should return filtered events with status code 200 OK`() {
        val name = "Test"
        val description = "Test"
        val dateFrom: LocalDateTime = _now
        val dateTo: LocalDateTime = _now.plusDays(1)
        val pattern: RecurringPattern = RecurringPattern.DAILY
        val filter = EventFilterDto(
            name = name,
            description = description,
            dateFrom = dateFrom,
            dateTo = dateTo,
            recurringPattern = pattern,
            calendarId = _dto.calendarId,
            categoryId = _dto.categoryId
        )
        val filteredDtos: List<EventDto> = listOf(_dto)

        whenever(_service.filter(filter = filter)).thenReturn(filteredDtos)

        val response: ResponseEntity<Page<EventDto>> = _controller.filter(
            name = name,
            description = description,
            dateFrom = dateFrom.toString(),
            dateTo = dateTo.toString(),
            recurringPattern = pattern.toString(),
            calendarId = _dto.calendarId,
            categoryId = _dto.categoryId,
            pageable = _pageable
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(filteredDtos.size.toLong(), response.body?.totalElements)
        assertEquals(filteredDtos, response.body?.content)

        verify(_service).filter(filter = filter)
    }

    @Test
    fun `should update event with status code 200 OK`() {
        val id: UUID = _dto.id!!
        val updated: EventDto = _dto.copy(id = UUID.randomUUID())

        whenever(_service.update(id = id, dto = updated)).thenReturn(updated)

        val response: ResponseEntity<EventDto> = _controller.update(id = id, dto = updated)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(updated, response.body)

        verify(_service).update(id = id, dto = updated)
    }

    @Test
    fun `should delete event with status code 204 No Content`() {
        val id: UUID = _dto.id!!
        doNothing().whenever(_service).delete(id = id)

        val response: ResponseEntity<Void> = _controller.delete(id = id)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        verify(_service).delete(id = id)
    }

}

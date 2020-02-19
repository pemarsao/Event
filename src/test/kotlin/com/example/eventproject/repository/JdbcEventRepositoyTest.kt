package com.example.eventproject.repository

import com.example.eventproject.form.EventForm
import com.example.eventproject.form.ProducerForm
import com.example.eventproject.model.Producer
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate

@Testcontainers
class JdbcEventRepositoyTest {

    @Container
    private val postgreSQLContainer = PostgreSQLContainer<Nothing>("postgres:9.6-alpine").apply {
        withDatabaseName("eventprojecttest")
        withUsername("username")
        withPassword("password")
    }

    private lateinit var eventRepository: JdbcEventRepository
    private lateinit var producerRepository: JdbcProducerRepository

    @BeforeEach
    fun start() {
        postgreSQLContainer.start()

        val dataSource = DataSourceBuilder.create()
                .driverClassName(postgreSQLContainer.driverClassName)
                .url(postgreSQLContainer.jdbcUrl)
                .username(postgreSQLContainer.username)
                .password(postgreSQLContainer.password)
                .build()

        Flyway.configure().dataSource(dataSource).load().migrate()

        val jdbcTemplate = JdbcTemplate(dataSource)

        eventRepository = JdbcEventRepository(jdbcTemplate)
        producerRepository = JdbcProducerRepository(jdbcTemplate)
    }

    @Test
    fun saveEvent() {

        val producer = saveProducer()

        val eventForm = producer?.id?.let {
            EventForm(
                    name = "name",
                    description = "description",
                    date = LocalDate.parse("2020-02-18"),
                    producer = it
            )
        }

        val event = eventForm?.let {
            eventRepository.saveEvent(it)
        }

        assertThat(event?.name).isEqualTo("name")
    }

    @Test
    fun updateEvent() {

        val producer = saveProducer()

        val eventForm = producer?.id?.let {
            EventForm(
                    name = "name",
                    description = "description",
                    date = LocalDate.parse("2020-02-18"),
                    producer = it
            )
        }

        val event = eventForm?.let {
            eventRepository.saveEvent(it)
        }

        eventForm?.apply {
            name = "updated name"
        }

        val updatedEvent = eventForm?.let { event?.id?.let { it1 -> eventRepository.updateEvent(it, it1) } }

        assertThat(updatedEvent?.name).isEqualTo("updated name")

    }

    private fun saveProducer(): Producer? {
        val producerForm = ProducerForm(
                name = "name",
                email = "email@email.com",
                document = "123456"
        )

        return producerRepository.saveProducer(producerForm)
    }
}

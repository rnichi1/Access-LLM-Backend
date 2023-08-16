package ch.uzh.ifi.access.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@Entity
class TaskInformation {
    @Id
    @GeneratedValue
    var id: Long? = null

    @JsonIgnore
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "task_id")
    var task: Task? = null

    @Column(nullable = false)
    var language: String? = null

    @Column(nullable = false)
    var title: String? = null

    @Column(nullable = false)
    var instructionsFile: String? = null
}

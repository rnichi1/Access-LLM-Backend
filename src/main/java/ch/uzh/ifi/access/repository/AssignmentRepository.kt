package ch.uzh.ifi.access.repository

import ch.uzh.ifi.access.model.Assignment
import ch.uzh.ifi.access.projections.AssignmentWorkspace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import java.util.*

interface AssignmentRepository : JpaRepository<Assignment?, Long?> {
    @PostFilter("hasRole(#courseSlug + '-assistant') or (hasRole(#courseSlug) and filterObject.published)")
    fun findByCourse_SlugOrderByOrdinalNumDesc(courseSlug: String?): List<AssignmentWorkspace>

    @PostAuthorize("hasRole(#courseSlug + '-assistant') or (hasRole(#courseSlug) and returnObject.present and returnObject.get().published)")
    fun findByCourse_SlugAndSlug(courseSlug: String?, assignmentSlug: String?): Optional<AssignmentWorkspace>
    fun getByCourse_SlugAndSlug(courseSlug: String?, assignmentSlug: String?): Optional<Assignment>
    fun existsByCourse_SlugAndSlug(courseSlug: String?, assignmentSlug: String?): Boolean
}
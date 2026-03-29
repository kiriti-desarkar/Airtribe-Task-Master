package com.tasktracker.service;

import com.tasktracker.dto.response.AttachmentResponse;
import com.tasktracker.entity.Attachment;
import com.tasktracker.entity.Task;
import com.tasktracker.entity.User;
import com.tasktracker.exception.FileStorageException;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.repository.AttachmentRepository;
import com.tasktracker.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing file attachments on tasks.
 */
@Service
public class AttachmentService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    /**
     * Uploads a file attachment to a task.
     */
    @Transactional
    public AttachmentResponse uploadAttachment(Long taskId, MultipartFile file, UserDetails userDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User currentUser = userService.getUserByUsername(userDetails.getUsername());

        // Validate file
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload an empty file");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Generate unique file name to avoid collisions
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Save file to disk
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Save attachment metadata to database
            Attachment attachment = Attachment.builder()
                    .fileName(originalFileName)
                    .filePath(storedFileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .task(task)
                    .uploadedBy(currentUser)
                    .build();

            attachment = attachmentRepository.save(attachment);

            return mapToAttachmentResponse(attachment);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }
    }

    /**
     * Lists all attachments for a task.
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getTaskAttachments(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", "id", taskId);
        }

        return attachmentRepository.findByTaskId(taskId).stream()
                .map(this::mapToAttachmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Downloads a file attachment.
     */
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long taskId, Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        if (!attachment.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Attachment", "id", attachmentId);
        }

        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + attachment.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + attachment.getFileName(), ex);
        }
    }

    /**
     * Gets the original filename for a download.
     */
    @Transactional(readOnly = true)
    public String getAttachmentFileName(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));
        return attachment.getFileName();
    }

    /**
     * Deletes a file attachment.
     */
    @Transactional
    public void deleteAttachment(Long taskId, Long attachmentId, UserDetails userDetails) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        if (!attachment.getTask().getId().equals(taskId)) {
            throw new ResourceNotFoundException("Attachment", "id", attachmentId);
        }

        // Delete file from disk
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize()
                    .resolve(attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log but don't fail — DB record will still be cleaned up
        }

        attachmentRepository.delete(attachment);
    }

    private AttachmentResponse mapToAttachmentResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedBy(userService.mapToUserResponse(attachment.getUploadedBy()))
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}

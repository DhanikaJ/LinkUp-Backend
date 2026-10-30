package controller;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import socket.ChatService;

@WebServlet("/MediaController")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10, // 10MB
        maxRequestSize = 1024 * 1024 * 50 // 50MB
)
public class MediaController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject responseObject = new JsonObject();

        try {
            String userId = request.getParameter("userId");
            Part mediaPart = request.getPart("media");

            if (mediaPart == null || userId == null) {
                responseObject.addProperty("success", false);
                responseObject.addProperty("message", "Missing required parameters");
                response.getWriter().write(responseObject.toString());
                return;
            }

            // Get content type
            String contentType = mediaPart.getContentType();
            String fileExtension = getFileExtension(mediaPart.getSubmittedFileName());

            // Validate file type
            if (!isValidFileType(contentType, fileExtension)) {
                responseObject.addProperty("success", false);
                responseObject.addProperty("message", "Invalid file type: " + contentType);
                response.getWriter().write(responseObject.toString());
                return;
            }

            // Get application path
            String appPath = request.getServletContext().getRealPath("");
            String mediaFolder = appPath.replace("build" + File.separator + "web",
                    "web" + File.separator + "chat-media");

            // Create user media folder
            File userMediaFolder = new File(mediaFolder, userId);
            if (!userMediaFolder.exists()) {
                userMediaFolder.mkdirs();
            }

            // Generate unique filename
            String fileName = "media_" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString() + "." + fileExtension;
            File mediaFile = new File(userMediaFolder, fileName);

            // Save file
            Files.copy(mediaPart.getInputStream(), mediaFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            // Build media URL
            String mediaUrl = ChatService.URL + "/LinkUp/chat-media/"
                    + userId + "/" + fileName;

            // Success response
            responseObject.addProperty("success", true);
            responseObject.addProperty("filePath", mediaUrl);
            responseObject.addProperty("fileName", fileName);
            responseObject.addProperty("fileType", contentType);
            responseObject.addProperty("timestamp", System.currentTimeMillis());
            responseObject.addProperty("message", "Media uploaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            responseObject.addProperty("success", false);
            responseObject.addProperty("message", "Upload failed: " + e.getMessage());
        }

        response.getWriter().write(responseObject.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("message", "Use POST method to upload media files");
        response.getWriter().write(responseObject.toString());
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "bin";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "bin";
    }

    /**
     * Validate if file type is allowed
     */
    private boolean isValidFileType(String contentType, String extension) {
        if (contentType == null) {
            return false;
        }

        // Allowed image types
        if (contentType.startsWith("image/")) {
            return extension.matches("(jpg|jpeg|png|gif|webp)");
        }

        // Allowed video types
        if (contentType.startsWith("video/")) {
            return extension.matches("(mp4|mov|avi|mkv)");
        }

        // Allowed audio types
        if (contentType.startsWith("audio/")) {
            return extension.matches("(mp3|wav|m4a|ogg)");
        }

        // Allowed document types
        if (contentType.contains("pdf")
                || contentType.contains("msword")
                || contentType.contains("document")
                || contentType.contains("spreadsheet")
                || contentType.contains("presentation")) {
            return extension.matches("(pdf|doc|docx|xls|xlsx|ppt|pptx)");
        }

        return false;
    }
}

package net.codejava.utea.media.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import net.codejava.utea.media.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final String baseFolder; // ví dụ: "utea"

    public CloudinaryServiceImpl(Cloudinary cloudinary, @Value("${cloudinary.folder:utea}") String baseFolder) {
        this.cloudinary = cloudinary;
        this.baseFolder = baseFolder;
    }

    @Override
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", baseFolder));
    }

    @Override
//	public Map<String, Object> upload(MultipartFile file, String folder, String publicId) throws IOException {
//		String target = (folder == null || folder.isBlank()) ? baseFolder : baseFolder + "/" + folder;
//
//		Map<String, Object> opts = new HashMap<>();
//		opts.put("folder", target);
//		if (publicId != null && !publicId.isBlank())
//			opts.put("public_id", publicId);
//		opts.put("overwrite", true);
//		return cloudinary.uploader().upload(file.getBytes(), opts);
//	}
    public Map<String, Object> upload(MultipartFile file, String folder, String publicId) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "utea/" + folder, // Thêm prefix "utea" cho toàn bộ dự án
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "auto"
        ));
    }

    @Override
    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}

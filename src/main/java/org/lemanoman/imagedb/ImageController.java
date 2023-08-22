package org.lemanoman.imagedb;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/image")
@CrossOrigin()
public class ImageController {

    @Value("${imagedb.upload.path}")
    private String path;

    private File getDefaultPath(Optional<String> subdir) {
        File defaultDir = new File(path);
        if (subdir.isPresent()) {
            defaultDir = new File(defaultDir, subdir.get());
        }

        if (defaultDir.exists()) {
            return defaultDir;
        }

        defaultDir.mkdirs();
        return defaultDir;
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadUserAvatarImage(@PathVariable String id) {
        try {
            File myfile = new File(getDefaultPath(Optional.of(id.substring(0, 2))), id);
            if (!myfile.exists()) {
                return ResponseEntity.status(404).body(null);
            }
            String extNum = id.substring(id.length() - 3);
            MediaExt mediaExt = numberToMediaExt(extNum);
            FileInputStream fis = new FileInputStream(myfile);
            MediaType mediaType = MediaType.IMAGE_PNG;
            if (mediaExt != null) {
                mediaType = MediaType.valueOf(mediaExt.getContentType());
            }


            return ResponseEntity.ok()
                    .contentLength(myfile.length())
                    .contentType(mediaType)
                    .body(new InputStreamResource(fis));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(404).body(null);

    }


    @RequestMapping(value = "thumb/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> downloadThumb(@PathVariable String id) {
        try {
            File myfile = new File(getDefaultPath(Optional.of(id.substring(0, 2))), id);
            if (!myfile.exists()) {
                return ResponseEntity.status(404).body(null);
            }
            String extNum = id.substring(id.length() - 3);
            MediaExt mediaExt = numberToMediaExt(extNum);
            FileInputStream fis = new FileInputStream(myfile);
            MediaType mediaType = MediaType.IMAGE_PNG;
            if (mediaExt != null) {
                mediaType = MediaType.valueOf(mediaExt.getContentType());
            }
            try (
                ByteArrayOutputStream os = new ByteArrayOutputStream();
            ){
                BufferedImage bufferedImage = cropImageSquare(fis);
                ImageIO.write(bufferedImage, "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                return ResponseEntity.ok()
                        .contentLength(myfile.length())
                        .contentType(mediaType)
                        .body(new InputStreamResource(is));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(404).body(null);

    }

    private BufferedImage cropImageSquare(InputStream inputStream) throws IOException {
        // Get a BufferedImage object from a byte array
        BufferedImage originalImage = ImageIO.read(inputStream);
        double height = originalImage.getHeight();
        double width = originalImage.getWidth();
        if(width>200){
            double scale = (200d/ width);
            int targetWidth =(int) Math.round(width * scale);
            int targetHeight = (int) Math.round( height * scale);
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = resizedImage.createGraphics();
            graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            graphics2D.dispose();
            return resizedImage;
        }
        return originalImage;


    }

    private Map<String, MediaExt> loadExts() {
        Map<String, MediaExt> extsMap = new HashMap<>();
        extsMap.put("01n", MediaExt.build("png", MediaType.IMAGE_PNG_VALUE));
        extsMap.put("cjl", MediaExt.build("svg", "image/svg+xml"));
        extsMap.put("y9i", MediaExt.build("gif", MediaType.IMAGE_GIF_VALUE));
        extsMap.put("avx", MediaExt.build("webp", "image/webp"));
        String[] jpgExts = {"jpg", "jpeg", "jfif", "pjpeg", "pjp"};
        int i = 0;
        for (String jpext : jpgExts) {
            extsMap.put("2fq" + i, MediaExt.build(jpext, MediaType.IMAGE_JPEG_VALUE));
            i++;
        }
        return extsMap;
    }

    private MediaExt numberToMediaExt(String extnum) {
        Map<String, MediaExt> map = loadExts();
        for (Map.Entry<String, MediaExt> entry : map.entrySet()) {
            if (entry.getKey().equals(extnum)) {
                return entry.getValue();
            }
        }
        return null;
    }


    private String extToNumber(String ext) {
        Map<String, MediaExt> map = loadExts();
        for (Map.Entry<String, MediaExt> entry : map.entrySet()) {
            if (entry.getValue().getExt().equals(ext)) {
                return entry.getKey();
            }
        }
        return "";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uplaodImage(@RequestParam("image") MultipartFile file)
            throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(file.getBytes());
        String uuid = UUID.randomUUID().toString();
        String defaultName = uuid + ".png";
        if (file.getOriginalFilename() != null) {
            defaultName = file.getOriginalFilename();
        }
        String[] points = defaultName.split("\\.");

        String ext = points[points.length - 1];

        String name = uuid + extToNumber(ext);
        // String name64 = new String(Base64.getEncoder().encode(name.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        File out = new File(getDefaultPath(
                Optional.of(name.substring(0, 2)))
                , name
        );
        FileOutputStream fos = new FileOutputStream(out);
        byte[] cache = new byte[1024];
        while (bis.read(cache) > -1) {
            fos.write(cache);
        }
        fos.flush();
        fos.close();
        bis.close();
        return ResponseEntity.status(HttpStatus.OK)
                .body(name);
    }
}
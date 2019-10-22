package com.app.reboot.service

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.app.reboot.exception.StorageException
import com.app.reboot.help.Final
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.DirectoryNotEmptyException
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.File




@Service
class StorageService {


    @Value("./media/")
    private val path: String? = null

    fun uploadFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw StorageException("Failed to store empty file")
        }
        try {
            val fileName = file.originalFilename
            val `is` = file.inputStream
            Files.copy(`is`, Paths.get(path!! + fileName!!), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            val msg = String.format("Failed to store file", file.name)
            throw StorageException(msg, e)
        }
    }

    fun uploadImageSetSize(file: MultipartFile, width:Int, height:Int) {
        if (file.isEmpty) {
            throw StorageException("Failed to store empty file")
        }
        try {
            val fileName = file.originalFilename ?: "img.jpg"
            val `is` = ByteArrayInputStream(resizeImageToWidth(file.bytes, getFileExtension(file.name), width))
            val bufferedImage = ImageIO.read(`is`)
            val resizedBufferedImage = bufferedImage.getSubimage(0, 0, width, height)
            val outputfile = File(path+fileName)
            ImageIO.write(resizedBufferedImage, getFileExtension(fileName), outputfile)
        } catch (e: IOException) {
            val msg = String.format("Failed to store file", file.name)
            throw StorageException(msg, e)
        }
    }

    fun uploadImageWithThumbnail(file: MultipartFile) {
        uploadImageSetSize(file, file.name , Final.imageWidth, Final.imageHeigh)
        uploadImageSetSize(file, "th_${file.name}", Final.thubnailWidth, Final.thubnailHeigh)
    }

    fun resizeImageToWidth(fileData: ByteArray, formatName: String, width:Int): ByteArray {
        val `in` = ByteArrayInputStream(fileData)
        try {
            val img = ImageIO.read(`in`)
            val w = width
            var h = img.height / (img.width / w)
            val scaledImage = img.getScaledInstance(w, h, Image.SCALE_SMOOTH)
            val imageBuff = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            imageBuff.graphics.drawImage(scaledImage, 0, 0, Color(0, 0, 0), null)
            val buffer = ByteArrayOutputStream()

            ImageIO.write(imageBuff, "jpg", buffer)

            return buffer.toByteArray()
        } catch (e: IOException) {
            throw ExceptionInInitializerError("IOException in scale image. Error: ${e.message}")
        }
    }

    fun uploadImageSetSize(file: MultipartFile, fileName:String, width:Int, height:Int) {
        if (file.isEmpty) {
            throw StorageException("Failed to store empty file")
        }
        try {
            val `is` = ByteArrayInputStream(resizeImageToWidth(file.bytes, getFileExtension(file.name), width))
            val bufferedImage = ImageIO.read(`is`)
            val resizedBufferedImage = bufferedImage.getSubimage(0, 0, width, height)
            val outputfile = File(path+fileName)
            ImageIO.write(resizedBufferedImage, getFileExtension(fileName), outputfile)
        } catch (e: IOException) {
            val msg = String.format("Failed to store file", file.name)
            throw StorageException(msg, e)
        }
    }

//    fun resizeImageToSize(fileData: ByteArray, formatName: String, width:Int, height:Int): ByteArray {
//        val `in` = ByteArrayInputStream(fileData)
//        try {
//            val img = ImageIO.read(`in`)
//            val scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
//            val imageBuff = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
//            imageBuff.graphics.drawImage(scaledImage, 0, 0, Color(0, 0, 0), null)
//            val buffer = ByteArrayOutputStream()
//
//            ImageIO.write(imageBuff, "jpg", buffer)
//
//            return buffer.toByteArray()
//        } catch (e: IOException) {
//            throw ExceptionInInitializerError("IOException in scale image. Error: ${e.message}")
//        }
//    }

    private fun getFileExtension(name: String): String {
        val lastIndexOf = name.lastIndexOf(".")
        return if (lastIndexOf == -1) {
            "" // empty extension
        } else name.substring(lastIndexOf+1)
    }


    fun resizeImageToSmall(fileData: ByteArray, currentSize: Long, maxSize: Long): ByteArray {
        if (currentSize < maxSize) {
            return fileData
        }
        val `in` = ByteArrayInputStream(fileData)
        try {

            val times = 2 //(int) (currentSize / maxSize);

            val img = ImageIO.read(`in`)
            var width = img.width / times
            var height = img.height / times
            if (height == 0) {
                height = width * img.height / img.width
            }
            if (width == 0) {
                width = height * img.width / img.height
            }
            val scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH)
            val imageBuff = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            imageBuff.graphics.drawImage(scaledImage, 0, 0, Color(0, 0, 0), null)

            val buffer = ByteArrayOutputStream()

            ImageIO.write(imageBuff, "jpg", buffer)

            return if (buffer.size() > maxSize) {
                this.resizeImageToSmall(buffer.toByteArray(), currentSize, maxSize)
            } else {
                buffer.toByteArray()
            }
        } catch (e: IOException) {
            throw ExceptionInInitializerError("IOException in scale image. Error: ${e.message}")
        }
    }


    fun removeFile(fileName:String) {
        try {
            Files.deleteIfExists(Paths.get(path!! + fileName!!))
        } catch (e: NoSuchFileException){
            throw StorageException("Failed file delete")
        } catch (e: DirectoryNotEmptyException){
            throw StorageException("Failed file delete")
        } catch (e: IOException  ){
            throw StorageException("Failed file delete")
        }

    }

}
package com.springbootproject.mydreamshop.controllers;

import com.springbootproject.mydreamshop.entity.Product;
import com.springbootproject.mydreamshop.entity.ProductDto;
import com.springbootproject.mydreamshop.services.ProductRepository;
import jakarta.validation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"","/"})
    public String showAllProducts (Model model){
//        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model){
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result){
        if(productDto.getImageFile().isEmpty()){
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }

        if(result.hasErrors()){
            return "products/createProduct";
        }
//      save image file into db
        MultipartFile image = productDto.getImageFile();
        Date createAt = new Date();
        String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

        try{
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)){
                Files.createDirectories(uploadPath);
            }
            try(InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream,Paths.get(uploadDir + storageFileName),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (Exception ex){
            System.out.println("Exception: " + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreateAt(createAt);
        product.setImageFileName(storageFileName);

        productRepository.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id){
        try{
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);
        }catch (Exception ex){
            System.out.println("Exception: " +ex.getMessage());
        }

        return "products/editProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result){
        try{
            Product product = productRepository.findById(id).get();
            model.addAttribute("product", product);

            if(result.hasErrors()){
                return "products/editProduct";
            }

            if(!productDto.getImageFile().isEmpty()){
                //delete previous img
                String uploadDir ="public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try{
                    Files.delete(oldImagePath);
                }
                catch (Exception ex){
                    System.out.println("Exception :" + ex.getMessage());
                }

                //save new img
                MultipartFile image = productDto.getImageFile();
                Date createAt = new Date();
                String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()){
                    Files.copy(inputStream,Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }
            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            productRepository.save(product);
        }catch (Exception ex){
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id){

        try{
            Product product = productRepository.findById(id).get();

            //delete product img
            Path imagePath = Paths.get("public/images" + product.getImageFileName());

            try {
                Files.delete(imagePath);
            }catch (Exception ex){
                System.out.println("Exception :" + ex.getMessage());
            }

            //delete product from db
            productRepository.delete(product);
        }catch (Exception ex){
            System.out.println("Exception :" + ex.getMessage());
        }

        return "redirect:/products";
    }
}

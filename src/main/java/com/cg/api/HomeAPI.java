package com.cg.api;


import com.cg.exception.AppUtils;
import com.cg.model.*;
import com.cg.model.dto.*;
import com.cg.repository.ImageRepository;
import com.cg.service.bill.IBillService;
import com.cg.service.cart.ICartService;
import com.cg.service.product.IProductService;
import com.cg.service.userService.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class HomeAPI {
    @Autowired
    private IProductService productService;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ICartService cartService;
    @Autowired
    private IBillService billService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppUtils appUtils;
    @GetMapping
    public ResponseEntity<?> showAll(){
        List<Product> products = productService.findAll();
        List<ProductResDTO> productResDTOs = products.stream()
                .map(product -> new ProductResDTO(
                        product.getId(),
                        product.getProductName(),
                        product.getDescription(),
                        product.getScore(),
                        product.getProductPrice(),
                        imageRepository.findImagesByProductId(product.getId())))
                .collect(Collectors.toList());

        return new ResponseEntity<>(productResDTOs, HttpStatus.OK);
    }
    @GetMapping("{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId){
        Optional<Product> product = productService.findById(productId);
        ProductResDTO productResDTO = product.orElseThrow().toProductResDTO();
        return new ResponseEntity<>(productResDTO,HttpStatus.OK);
    }
    @PostMapping("/cart/{idCustomer}")
    public ResponseEntity<?> addProToCart(@PathVariable Long idCustomer,@RequestBody CartDetailReqDTO cartDetailReqDTO){
        Optional<Cart> cart = cartService.findByIdCustomer(idCustomer);

        CartDetail cartDetail = cartDetailReqDTO.toCartDetail();

        if (cart.isPresent()) {

            cartDetail.setCart(cart.get());

            if (cartService.existsByIdProduct(cartDetail.getProduct().getId(),idCustomer) > 0){

                cartService.saveCartDetailIsExitWithProduct(cartDetail);
            } else {

                cartService.saveCartDetail(cartDetail);
            }
        } else {
            Cart newCart = cartDetailReqDTO.toCart();
            newCart.setCustomer(new Customer(idCustomer));
            cartService.save(newCart);

            cartDetail.setCart(newCart);

            cartService.saveCartDetail(cartDetail);
        }
        return new ResponseEntity<>(cartDetailReqDTO,HttpStatus.OK);
    }
    @GetMapping("/cart/{idCustomer}")
    public ResponseEntity<?> getCarts(@PathVariable Long idCustomer){

        List<CartDetailResDTO> cartDetails = cartService.getAllByCustomer_Id(idCustomer);

        return new ResponseEntity<>(cartDetails,HttpStatus.OK);
    }

    @GetMapping("delete/{idCartDetail}")
    public ResponseEntity<?> deleteProductFromCart(@PathVariable Long idCartDetail){

        cartService.deleteCartDetail(idCartDetail);

        return new ResponseEntity<>("OK",HttpStatus.OK);
    }
    @GetMapping("/cartDetail/{idCustomer}")
    public  ResponseEntity<?> getCountDetail(@PathVariable Long idCustomer){

       Long countCartDetailByCustomer =  cartService.getCountDetail(idCustomer);

       return new ResponseEntity<>(countCartDetailByCustomer,HttpStatus.OK);
    }

    @GetMapping("/customer/{customerID}")
    public  ResponseEntity<?> getCustomerByID(@PathVariable Long customerID){

        Customer customer = userService.findById(customerID);

        CustomerResDTO customerResDTO = customer.toCustomerResDTO();

        return new ResponseEntity<>(customerResDTO,HttpStatus.OK);
    }
    @PatchMapping("/customer/{customerID}")
    public  ResponseEntity<?> updateCustomerByID(@PathVariable Long customerID, @Valid @RequestBody CustomerReqDTO customerReqDTO, BindingResult bindingResult){


        if (bindingResult.hasFieldErrors()){
            return appUtils.mapErrorToResponse(bindingResult);
        }

        Customer customer = userService.findById(customerID);

        customer.setName(customerReqDTO.getName());

        customer.setDob(customerReqDTO.getDob());

        customer.setEmail(customerReqDTO.getEmail());

        customer.setPhone(customerReqDTO.getPhone());

        userService.save(customer);

        return new ResponseEntity<>(customer,HttpStatus.OK);
    }
}

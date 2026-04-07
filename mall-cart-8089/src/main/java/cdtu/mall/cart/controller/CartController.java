package cdtu.mall.cart.controller;

import cdtu.mall.cart.pojo.Cart;
import cdtu.mall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     *- 请求方式：新增，Post
     * - 请求路径：/cart ，这个其实是Zuul路由的路径，我们可以不管
     * - 请求参数：Json对象，包含skuId和num属性
     * - 返回结果：无
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询登录用户的购物车信息
     * @return
     */
    @GetMapping("/queryCarts")
    public ResponseEntity<List<Cart>> queryCartList(){

        List<Cart> carts = this.cartService.queryCartList();
        if(CollectionUtils.isEmpty(carts)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 更新购物车商品数量
     * @param cart
     * @return
     */
    @PostMapping("/updataNum")
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseEntity.noContent().build();
    }

    /**
     * 通过id删除1条购物车数据
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId){
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
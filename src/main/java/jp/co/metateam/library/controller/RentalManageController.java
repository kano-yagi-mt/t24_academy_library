package jp.co.metateam.library.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
 
import jakarta.validation.Valid;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.values.AuthorizationTypes;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.service.StockService;
 
import lombok.extern.log4j.Log4j2;
import java.util.List;
 
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
 
    @Autowired
    public RentalManageController(
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList= this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList",rentalManageList);
        // 貸出一覧画面に遷移
        return "rental/index";
    }
 
    @GetMapping("/rental/add")
    public String add(Model model) {
     //テーブルから情報を持ってくる
     List<RentalManage> rentalManageList= this.rentalManageService.findAll();
     List<Stock> stockList = this.stockService.findStockAvailableAll();
     List<Account> accountList= this.accountService.findAll();
 
     //モデル
     model.addAttribute("rentalStatus",RentalStatus.values());
     model.addAttribute("stockList",stockList);
     model.addAttribute("accounts",accountList);
 
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }
 
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }
 
    @GetMapping ("/rental/{id}/edit")
    //登録されているデータを取得
     public String edit(@PathVariable("id") String id,Model model) {//URLパスからidパラメータを取得して文字列化・モデルオブジェクトを引数として受け取り、Viewに渡す
        List<Stock> stockList = this.stockService.findStockAvailableAll();//stockListの取得:stockServiceクラスからfindStockAvailableAll()メソッドを呼び出す
        List<Account> accountList= this.accountService.findAll();//accountListの取得:accountServiceクラスからfindAllメソッドを呼び出す
 
        //モデル
        model.addAttribute("rentalStatus",RentalStatus.values());//modelにrentalStatusという名前でRentalStatus.values()の結果を追加
        model.addAttribute("stockList",stockList);//modelにstockListという名前でstockListの値を追加
        model.addAttribute("accounts",accountList);//modelにaccountsという名前でaccountListの値を追加
        //ここでmodelオブジェクトに追加されたデータがViewに渡され、画面に表示される
       
       
           if (!model.containsAttribute("rentalManageDto")) {//modelにrentalManageDtoが含まれているかチェック
              RentalManageDto rentalManageDto = new RentalManageDto();//もし含まれていなければ、RentalManageDtoを新しく作成
              RentalManage rentalManage= this.rentalManageService.findById(Long.valueOf(id));//idに紐づいたrentalManageオブジェクトを取得
             
 
              model.addAttribute("rentalManageList",rentalManage);//rentalManageListという名前でrentalManageのデータをmodelに追加
             
              rentalManageDto.setId(rentalManage.getId());//rentalManageDtoオブジェクトのIdにrentalManageのIdを設定
              rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
              rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
              rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
              rentalManageDto.setStatus(rentalManage.getStatus());
              rentalManageDto.setStockId(rentalManage.getStock().getId());
 
              model.addAttribute("rentalManageDto", rentalManageDto);
              //rentalManageオブジェクトからrentalManageDtoに必要なデータを渡している
 
           }
   
           return "rental/edit";//ここに返す
       }
 
    @PostMapping("/rental/{id}/edit")//Postのリクエストがあった場合にこの処理を行う

       public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra,Model model) {
         //リクエストパスのidをString型で受け取る・@Validでバリデーションチェックをしけ結果をBindingResultに格納・RentalManageDtoオブジェクトを@ModelAttributeとして受け取る

          
          try {

              RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));//変更前の貸出情報
              Integer preStatus = rentalManage.getStatus();
              Integer newStatus = rentalManageDto.getStatus();
              String statusError = rentalManageDto.isValidStatus(preStatus,newStatus);//rentalManageDtoの貸出ステータスが有効かどうか

                if(statusError != null){//Dtoで行った貸出ステータスのバリデーションチェックでエラーがあった場合
                    FieldError fieldError = (new FieldError("rentalManageDto","status",statusError));
                    //statusError.get()から取得したエラーメッセージをfieldErrorに入れる
                    result.addError(fieldError);//resultにエラーの情報を入れる
                }

              if (result.hasErrors()) {//resultにエラーの情報がある場合
                     throw new Exception("Validation error.");//エラーを投げる
              }
               // 登録処理
              rentalManageService.update(Long.valueOf(id), rentalManageDto);//指定されたidのrentalManageオブジェクトを更新
   
              return "redirect:/rental/index";//更新された場合は指定されたリダイレクト先にデータを返す

            } catch (Exception e) {
               log.error(e.getMessage());//エラーがあった場合はエラーメッセージを表示するようにする
   
               ra.addFlashAttribute("rentalManageDto", rentalManageDto);
               ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

               List<Stock> stockList = this.stockService.findStockAvailableAll();//stockListの取得:stockServiceクラスからfindStockAvailableAll()メソッドを呼び出す
               List<Account> accountList= this.accountService.findAll();//accountListの取得:accountServiceクラスからfindAllメソッドを呼び出す
 
               //モデル
               model.addAttribute("rentalStatus",RentalStatus.values());
               model.addAttribute("stockList",stockList);
               model.addAttribute("accounts",accountList);
               
               return "rental/edit";//書籍一覧画面に戻る
           }
        }
 
 
    }
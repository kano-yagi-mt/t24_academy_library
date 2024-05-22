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
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.service.StockService;
 
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

 
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController{
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
    private final RentalManageRepository rentalManageRepository;
 
    @Autowired
    public RentalManageController(
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService,
        RentalManageRepository rentalManageRepository
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
        this.rentalManageRepository = rentalManageRepository;
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model){
        // 貸出管理テーブルから全件取得
        List<RentalManage> rentalManageList= this.rentalManageService.findAll();
        // 貸出一覧画面に渡すデータをmodelに追加
        model.addAttribute("rentalManageList", rentalManageList);
        // 貸出一覧画面に遷移
        return "rental/index";
    }
 
    @GetMapping("/rental/add")
    public String add(Model model){
        //テーブルから情報を持ってくる
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accountList= this.accountService.findAll();
 
     //モデル
        model.addAttribute("rentalStatus", RentalStatus.values());
        model.addAttribute("stockList", stockList);
        model.addAttribute("accounts", accountList);
 
        if (!model.containsAttribute("rentalManageDto")){
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }
 
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra){
        try {
            String errorMessage = checkInventoryStatus(rentalManageDto.getStockId());
        if (errorMessage != null){
            result.addError(new FieldError("rentalManageDto", "stockId", errorMessage));
        }
            String errorText = Datecheck(rentalManageDto, rentalManageDto.getStockId());
            if (errorText != null){
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", errorText));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", errorText));
            }
            
            if (result.hasErrors()){
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";

        } catch (Exception e){
                log.error(e.getMessage());
 
                ra.addFlashAttribute("rentalManageDto", rentalManageDto);
                ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }
    /**
    * 登録されているデータを取得
    */
    @GetMapping ("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model, @RequestParam(name = "errorMessage", required = false) String errorMessage){
    //URLパスからidパラメータを取得して文字列化・モデルオブジェクトを引数として受け取り、Viewに渡す

        List<RentalManage> rentalManageList = this.rentalManageService.findAll();
        List<Stock> stockList = this.stockService.findStockAvailableAll();//stockListの取得:stockServiceクラスからfindStockAvailableAll()メソッドを呼び出す
        List<Account> accountList= this.accountService.findAll();//accountListの取得:accountServiceクラスからfindAllメソッドを呼び出す
 
        //モデル
        model.addAttribute("rentalStatus",RentalStatus.values());//modelにrentalStatusという名前でRentalStatus.values()の結果を追加
        model.addAttribute("stockList", stockList);//modelにstockListという名前でstockListの値を追加
        model.addAttribute("accounts", accountList);//modelにaccountsという名前でaccountListの値を追加
        //ここでmodelオブジェクトに追加されたデータがViewに渡され、画面に表示される

        model.addAttribute("rentalManageList", rentalManageList);
        model.addAttribute("rentalStockStatus", StockStatus.values());

        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
       
       
        if (!model.containsAttribute("rentalManageDto")){//modelにrentalManageDtoが含まれているかチェック
            RentalManageDto rentalManageDto = new RentalManageDto();//もし含まれていなければ、RentalManageDtoを新しく作成
            RentalManage rentalManage= this.rentalManageService.findById(Long.valueOf(id));//idに紐づいたrentalManageオブジェクトを取得
             
 
            model.addAttribute("rentalManageList",rentalManage);//rentalManageListという名前でrentalManageのデータをmodelに追加
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            model.addAttribute("rentalManageDto", rentalManageDto);//rentalManageオブジェクトからrentalManageDtoに必要なデータを渡している
 
        }
   
            return "rental/edit";
    }
    /**
    * Postのリクエストがあった場合にこの処理を行う
    */
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra, Model model){
         //リクエストパスのidをString型で受け取る・@Validでバリデーションチェックをしけ結果をBindingResultに格納・RentalManageDtoオブジェクトを@ModelAttributeとして受け取る

        try {

            String errorMessage = checkInventoryStatus(rentalManageDto.getStockId());
            if (errorMessage != null) {
                result.addError(new FieldError("rentalManageDto", "stockId", errorMessage));
            }
 
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            
            String dateError = this.Datecheck(rentalManageDto,rentalManageDto.getStockId(),rentalManageDto.getId());
            if (dateError != null){
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", dateError));
                result.addError(new FieldError("rentalManageDto", "expectedReturnOn", dateError));
            }

            if (result.hasErrors()){
                //バリデーションチェックエラー
                model.addAttribute("errorMessage","入力内容にエラーがあります");
                //バリデーションエラーがある場合は編集画面に戻る
                prepareModelAttributes(model);
                return "rental/edit";
            }
            
            RentalManage rental = this.rentalManageService.findById(Long.valueOf(id));
            if(rental == null){
                model.addAttribute("errorMessage", "指定された貸出情報が見つかりません");
                //貸出情報が見つからない場合は編集画面に戻る
                prepareModelAttributes(model);
                return "rental/edit";
            }

            //貸出情報の貸出ステータスをチェック
            Optional<String> statusError = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if(statusError.isPresent()){
                //Dtoで行った貸出ステータスのバリデーションチェックでエラーがあった場合
                FieldError fieldError = new FieldError("rentalManageDto","status", statusError.get());
                //statusErrorから取得したエラーメッセージをfieldErrorに入れる
                result.addError(fieldError);
                //resultにエラーの情報を入れる
                 throw new Exception("Validation error");
                //エラーを投げる
            }

            //貸出予定日のバリデーションチェック
            if (rentalManage.getStatus() == RentalStatus.RENT_WAIT.getValue() &&
                rentalManageDto.getStatus() == RentalStatus.RENTAlING.getValue()){
                    
                    if (rentalManageDto.isValidRentalDate() != 0){
                        FieldError fieldError = new FieldError("rentalManageDto","expectedRentalOn", "貸出予定日は現在の日付に設定してください");
                        result.addError(fieldError);
                        throw new Exception("Validation error");
                    }

            //返却予定日のバリデーションチェック
            }else if (rentalManage.getStatus() == RentalStatus.RENTAlING.getValue() &&
               rentalManageDto.getStatus() == RentalStatus.RETURNED.getValue()){
                
                if(rentalManageDto.isValidReturnDate() != 0){
                    FieldError fieldError = new FieldError("rentalManageDto","expectedReturnOn", "返却予定日は現在の日付に設定してください");
                    result.addError(fieldError);
                    throw new Exception("Validation error");
                }
            }

            // 更新処理
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);
            return "redirect:/rental/index";

        } catch (Exception e) {
            // エラーが発生した場合の処理
            log.error("更新処理中にエラーが発生しました: " + e.getMessage());
            model.addAttribute("errorMessage", "更新処理中にエラーが発生しました");
 
            // 在庫管理番号の再取得とモデルへの追加
            List<Stock> stockList = this.stockService.findStockAvailableAll();
            model.addAttribute("stockList", stockList);
 
            // アカウント情報の再取得とモデルへの追加
            List<Account> accounts = this.accountService.findAll();
            model.addAttribute("accounts", accounts);
 
            // 貸出ステータスの追加
            model.addAttribute("rentalStatus", RentalStatus.values());
             
            // 貸出情報の取得
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            // 貸出情報が存在する場合は、貸出管理情報を再度モデルに追加
            if (rentalManage != null) {
                model.addAttribute("rentalManageDto", rentalManageDto);
                ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
            }
                return "rental/edit";
        }
    }
 
    private String checkInventoryStatus(String id) {
        // 在庫ステータスを確認するロジックを記述
        Stock stock = this.stockService.findById(id);
        if (stock.getStatus() == 0) {
            return null; // 利用可の場合はエラーメッセージなし
            } else {
                return "この本は利用できません"; // 利用不可の場合はエラーメッセージを返す
            }
    }
 
    public String Datecheck(RentalManageDto rentalManageDto,String id){
        List<RentalManage> rentalAvailable = this.rentalManageService.findByStockIdAndStatusIn(id);
        if (rentalAvailable != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalAvailable) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())&& rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())) {
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    }
 
    public String Datecheck(RentalManageDto rentalManageDto,String id,Long rentalId){
        List <RentalManage> rentalManages = this.rentalManageService.findByStockIdAndStatusIn(id, rentalId);
        if (rentalManages != null) {
            // ループ処理
            for (RentalManage rentalManage : rentalManages) {
                if (rentalManage.getExpectedReturnOn().after(rentalManageDto.getExpectedRentalOn())&& rentalManage.getExpectedRentalOn().before(rentalManageDto.getExpectedReturnOn())){
                    return "貸出期間が重複しています";
                }
            }
            return null;
        } else {
            return null;
        }
    }
 
    public void prepareModelAttributes(Model model){
        
        // バリデーションエラーがある場合は編集画面に戻るためのデータを取得
        List<Stock> stockList = this.stockService.findStockAvailableAll();
        List<Account> accounts = this.accountService.findAll();
 
        // モデルにデータを追加
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
    }
}       
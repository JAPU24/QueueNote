package com.adrian.queuenote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class AppStrings(
    // Common
    val app_name: String = "QueueNote",
    val back: String = "Volver",
    val cancel: String = "Cancelar",
    val delete: String = "Eliminar",
    val save: String = "Guardar",
    val continue_btn: String = "Continuar",
    val loading: String = "Cargando...",
    val title_label: String = "Título *",
    val description_label: String = "Descripción (opcional)",
    val status_label: String = "Estado",
    val due_date_selection: String = "Fecha límite",
    val select_btn: String = "Seleccionar",
    val clear_date: String = "Quitar fecha",
    val no_due_date: String = "Sin fecha límite",

    // Navigation / Drawer
    val home_title: String = "Menú principal",
    val inventory_title: String = "Inventario",
    val profile_title: String = "Mi perfil",
    val settings_title: String = "Ajustes",
    val logout: String = "Cerrar sesión",

    // Splash
    val splash_subtitle: String = "Registro personal de esperas y procesos",

    // Login
    val login_title: String = "Iniciar sesión",
    val email_label: String = "Correo",
    val password_label: String = "Contraseña",
    val show_pass: String = "Ver",
    val hide_pass: String = "Ocultar",
    val forgot_pass_link: String = "¿Olvidaste tu contraseña?",
    val login_btn: String = "Entrar",
    val login_github: String = "Continuar con GitHub",
    val create_account_btn: String = "Crear cuenta",
    val invalid_email: String = "Correo no válido",

    // Register
    val register_title: String = "Registro",
    val name_label: String = "Nombre",
    val confirm_password_label: String = "Confirmar contraseña",
    val account_type: String = "Tipo de cuenta",
    val private_label: String = "Privada",
    val public_label: String = "Pública",
    val pass_mismatch: String = "Las contraseñas no coinciden",
    val back_to_login: String = "Volver a Login",

    // Forgot Password
    val forgot_title: String = "Recuperar contraseña",
    val forgot_desc: String = "Escribe tu correo y te enviaremos un enlace real.",
    val send_link: String = "Enviar enlace",

    // Create / Edit Process
    val create_process_title: String = "Crear proceso",
    val edit_process_title: String = "Editar proceso",

    // Home / Processes
    val no_processes: String = "No hay procesos",
    val add_first_process: String = "Toca el + para crear tu primer proceso.",
    val filter_all: String = "Todos",
    val filter_pending: String = "Pendiente",
    val filter_waiting: String = "En espera",
    val filter_completed: String = "Completado",
    val due_date_label: String = "Vence:",
    val progress_label: String = "Progreso:",
    val reopen_btn: String = "Reabrir",
    val delete_process_confirm: String = "¿Seguro que deseas eliminar el proceso?",

    // Task Detail
    val detail_title: String = "Detalle del proceso",
    val add_group: String = "Agregar grupo",
    val add_subtask: String = "Agregar subtarea",
    val no_subtasks: String = "Sin subtareas todavía.",
    val delete_subtask_confirm: String = "¿Seguro que deseas eliminar esta subtarea?",
    val group_name_label: String = "Nombre del grupo",
    val subtask_text_label: String = "Texto de la subtarea",
    val add_btn: String = "Agregar",

    // Inventory
    val search_placeholder: String = "Buscar artículo...",
    val items_label: String = "Artículos",
    val stock: String = "Stock",
    val total_stock: String = "Stock Total",
    val sell_value: String = "Valor Venta",
    val investment: String = "Inversión",
    val profit: String = "Beneficio",
    val cost: String = "Costo",
    val price: String = "Precio",
    val cant_label: String = "Cant",
    val export_success: String = "Exportado a Descargas",
    val dashboard_title: String = "Dashboard de Inventario",

    // Settings
    val appearance: String = "Apariencia",
    val language_label: String = "Idioma",
    val auto_system: String = "Automático (Sistema)",
    val light_mode: String = "Modo Claro",
    val dark_mode: String = "Modo Oscuro",
    val spanish: String = "Español",
    val english: String = "Inglés",

    // Change Password
    val change_pass_title: String = "Cambiar contraseña",
    val current_pass_label: String = "Contraseña actual",
    val new_pass_label: String = "Nueva contraseña",
    val confirm_pass_label: String = "Confirmar nueva contraseña",
    val update_pass_btn: String = "Actualizar contraseña"
)

data class TranslationResponse(
    val es: AppStrings,
    val en: AppStrings
)

interface TranslationApi {
    @GET("adrian-translations.json")
    suspend fun getTranslations(): TranslationResponse
}

class TranslationRepository {
    suspend fun fetchStrings(lang: String): AppStrings {
        return try {
            if (lang == "en") getEnglishStrings() else getSpanishStrings()
        } catch (e: Exception) {
            if (lang == "en") getEnglishStrings() else getSpanishStrings()
        }
    }

    private fun getSpanishStrings() = AppStrings()

    private fun getEnglishStrings() = AppStrings(
        back = "Back",
        cancel = "Cancel",
        delete = "Delete",
        save = "Save",
        continue_btn = "Continue",
        loading = "Loading...",
        title_label = "Title *",
        description_label = "Description (optional)",
        status_label = "Status",
        due_date_selection = "Due Date",
        select_btn = "Select",
        clear_date = "Clear Date",
        no_due_date = "No due date",
        home_title = "Main Menu",
        inventory_title = "Inventory",
        profile_title = "My Profile",
        settings_title = "Settings",
        logout = "Log Out",
        splash_subtitle = "Personal record of waits and processes",
        login_title = "Sign In",
        email_label = "Email",
        password_label = "Password",
        show_pass = "Show",
        hide_pass = "Hide",
        forgot_pass_link = "Forgot your password?",
        login_btn = "Login",
        login_github = "Continue with GitHub",
        create_account_btn = "Create Account",
        invalid_email = "Invalid email",
        register_title = "Register",
        name_label = "Name",
        confirm_password_label = "Confirm Password",
        account_type = "Account Type",
        private_label = "Private",
        public_label = "Public",
        pass_mismatch = "Passwords do not match",
        back_to_login = "Back to Login",
        forgot_title = "Recover Password",
        forgot_desc = "Enter your email and we will send you a real link.",
        send_link = "Send Link",
        create_process_title = "Create Process",
        edit_process_title = "Edit Process",
        no_processes = "No processes",
        add_first_process = "Tap + to create your first process.",
        filter_all = "All",
        filter_pending = "Pending",
        filter_waiting = "Waiting",
        filter_completed = "Completed",
        due_date_label = "Due:",
        progress_label = "Progress:",
        reopen_btn = "Reopen",
        delete_process_confirm = "Are you sure you want to delete the process?",
        detail_title = "Process Detail",
        add_group = "Add Group",
        add_subtask = "Add Subtask",
        no_subtasks = "No subtasks yet.",
        delete_subtask_confirm = "Are you sure you want to delete this subtask?",
        group_name_label = "Group Name",
        subtask_text_label = "Subtask Text",
        add_btn = "Add",
        search_placeholder = "Search article...",
        items_label = "Items",
        stock = "Stock",
        total_stock = "Total Stock",
        sell_value = "Sell Value",
        investment = "Investment",
        profit = "Profit",
        cost = "Cost",
        price = "Price",
        cant_label = "Qty",
        export_success = "Exported to Downloads",
        dashboard_title = "Inventory Dashboard",
        appearance = "Appearance",
        language_label = "Language",
        auto_system = "Automatic (System)",
        light_mode = "Light Mode",
        dark_mode = "Dark Mode",
        spanish = "Spanish",
        english = "English",
        change_pass_title = "Change Password",
        current_pass_label = "Current Password",
        new_pass_label = "New Password",
        confirm_pass_label = "Confirm New Password",
        update_pass_btn = "Update Password"
    )
}

import 'package:flutter/material.dart';

class ProductScreen extends StatefulWidget {
  const ProductScreen({super.key});

  @override
  State<ProductScreen> createState() => _ProductScreenState();
}

class _ProductScreenState extends State<ProductScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  
  // Ürün formu için
  final _productFormKey = GlobalKey<FormState>();
  final _productNameController = TextEditingController();
  final _productPriceController = TextEditingController();
  final _productDescriptionController = TextEditingController();
  String _selectedProductCategory = 'Ana Yemek';
  String _selectedProductStatus = 'Aktif';
  
  // İçerik formu için
  final _ingredientFormKey = GlobalKey<FormState>();
  final _ingredientNameController = TextEditingController();
  final _ingredientUnitController = TextEditingController();
  final _ingredientStockController = TextEditingController();
  final _ingredientMinStockController = TextEditingController();
  
  // Ürün tarifi için
  final _recipeIngredientController = TextEditingController();
  final _recipeQuantityController = TextEditingController();
  String _selectedRecipeIngredient = '';
  
  final List<String> _productCategories = [
    'Ana Yemek',
    'Çorba',
    'Salata',
    'Tatlı',
    'İçecek',
    'Kahvaltı',
    'Ara Sıcak',
    'Meze',
  ];
  
  final List<String> _productStatuses = ['Aktif', 'Pasif'];
  final List<String> _units = ['kg', 'adet', 'ml', 'gr'];
  
  // Örnek veriler
  final List<Map<String, dynamic>> _products = [
    {
      'id': 'P001',
      'name': 'Karışık Pizza',
      'description': 'Sucuk, sosis, mantar, biber, mısır ile',
      'price': 85.50,
      'category': 'Ana Yemek',
      'status': 'Aktif',
      'ingredients': ['Hamur', 'Sucuk', 'Sosis', 'Mantar', 'Biber', 'Mısır'],
    },
    {
      'id': 'P002',
      'name': 'Mercimek Çorbası',
      'description': 'Geleneksel Türk mutfağından',
      'price': 25.00,
      'category': 'Çorba',
      'status': 'Aktif',
      'ingredients': ['Mercimek', 'Soğan', 'Havuç', 'Baharatlar'],
    },
  ];
  
  final List<Map<String, dynamic>> _ingredients = [
    {
      'id': 'I001',
      'name': 'Hamur',
      'unit': 'kg',
      'currentStock': 50.0,
      'minStock': 10.0,
    },
    {
      'id': 'I002',
      'name': 'Sucuk',
      'unit': 'kg',
      'currentStock': 25.0,
      'minStock': 5.0,
    },
    {
      'id': 'I003',
      'name': 'Mercimek',
      'unit': 'kg',
      'currentStock': 30.0,
      'minStock': 8.0,
    },
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Ürün & Stok Yönetimi'),
        backgroundColor: const Color(0xFF9C27B0),
        foregroundColor: Colors.white,
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Ürünler'),
            Tab(text: 'İçerikler'),
          ],
          indicatorColor: Colors.white,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          _buildProductsTab(),
          _buildIngredientsTab(),
        ],
      ),
    );
  }

  Widget _buildProductsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Ürün ekleme formu
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20.0),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  spreadRadius: 1,
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Form(
              key: _productFormKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Icon(
                        Icons.restaurant_menu,
                        color: const Color(0xFF9C27B0),
                        size: 28,
                      ),
                      const SizedBox(width: 12),
                      const Expanded(
                        child: Text(
                          'Yeni Ürün Ekle',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  
                  // Ürün adı ve fiyat
                  Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: _productNameController,
                          decoration: const InputDecoration(
                            labelText: 'Ürün Adı',
                            border: OutlineInputBorder(),
                            prefixIcon: Icon(Icons.shopping_cart),
                          ),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: TextFormField(
                          controller: _productPriceController,
                          decoration: const InputDecoration(
                            labelText: 'Fiyat (₺)',
                            border: OutlineInputBorder(),
                            prefixIcon: Icon(Icons.attach_money),
                          ),
                          keyboardType: TextInputType.number,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Kategori ve durum
                  Column(
                    children: [
                      DropdownButtonFormField<String>(
                        value: _selectedProductCategory,
                        decoration: const InputDecoration(
                          labelText: 'Kategori',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.category),
                        ),
                        items: _productCategories.map((String category) {
                          return DropdownMenuItem<String>(
                            value: category,
                            child: Text(category),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedProductCategory = newValue!;
                          });
                        },
                      ),
                      const SizedBox(height: 16),
                      DropdownButtonFormField<String>(
                        value: _selectedProductStatus,
                        decoration: const InputDecoration(
                          labelText: 'Durum',
                          border: OutlineInputBorder(),
                          prefixIcon: Icon(Icons.info),
                        ),
                        items: _productStatuses.map((String status) {
                          return DropdownMenuItem<String>(
                            value: status,
                            child: Text(status),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedProductStatus = newValue!;
                          });
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Açıklama
                  TextFormField(
                    controller: _productDescriptionController,
                    decoration: const InputDecoration(
                      labelText: 'Açıklama',
                      border: OutlineInputBorder(),
                      prefixIcon: Icon(Icons.description),
                    ),
                    maxLines: 3,
                  ),
                  const SizedBox(height: 20),
                  
                  // Tarif malzemeleri
                  const Text(
                    'Tarif:',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Tarife Malzeme Ekle:',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Column(
                    children: [
                      DropdownButtonFormField<String>(
                        value: _selectedRecipeIngredient.isNotEmpty ? _selectedRecipeIngredient : null,
                        decoration: const InputDecoration(
                          labelText: 'İçerik Seçin',
                          border: OutlineInputBorder(),
                        ),
                        items: _ingredients.map((ingredient) {
                          return DropdownMenuItem<String>(
                            value: ingredient['name'],
                            child: Text(ingredient['name']),
                          );
                        }).toList(),
                        onChanged: (String? newValue) {
                          setState(() {
                            _selectedRecipeIngredient = newValue ?? '';
                          });
                        },
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(
                            child: TextFormField(
                              controller: _recipeQuantityController,
                              decoration: const InputDecoration(
                                labelText: 'Miktar',
                                border: OutlineInputBorder(),
                              ),
                              keyboardType: TextInputType.number,
                            ),
                          ),
                          const SizedBox(width: 16),
                          ElevatedButton(
                            onPressed: _addRecipeIngredient,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.green,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                            ),
                            child: const Text('Ekle'),
                          ),
                        ],
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  
                  // Ürün ekle butonu
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: _addProduct,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFF9C27B0),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: const Text(
                        'Ekle',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          
          const SizedBox(height: 20),
          
          // Ürün listesi
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20.0),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  spreadRadius: 1,
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Mevcut Ürünler',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                ..._products.map((product) => _buildProductCard(product)).toList(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildIngredientsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // İçerik ekleme formu
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20.0),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  spreadRadius: 1,
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Form(
              key: _ingredientFormKey,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Yeni İçerik Ekle',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 20),
                  
                  // İçerik adı, birim, başlangıç stoğu
                  Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: _ingredientNameController,
                          decoration: const InputDecoration(
                            labelText: 'İçerik Adı',
                            border: OutlineInputBorder(),
                          ),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: DropdownButtonFormField<String>(
                          value: _ingredients.isNotEmpty ? _ingredients.first['unit'] : null,
                          decoration: const InputDecoration(
                            labelText: 'Birim (kg, adet, ml)',
                            border: OutlineInputBorder(),
                          ),
                          items: _units.map((unit) {
                            return DropdownMenuItem<String>(
                              value: unit,
                              child: Text(unit),
                            );
                          }).toList(),
                          onChanged: (String? newValue) {
                            // Burada birim seçimi yapılabilir
                          },
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: TextFormField(
                          controller: _ingredientStockController,
                          decoration: const InputDecoration(
                            labelText: 'Başlangıç Stoğu',
                            border: OutlineInputBorder(),
                          ),
                          keyboardType: TextInputType.number,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  
                  // Minimum stok ve ekle butonu
                  Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: _ingredientMinStockController,
                          decoration: const InputDecoration(
                            labelText: 'Minimum Stok',
                            border: OutlineInputBorder(),
                          ),
                          keyboardType: TextInputType.number,
                        ),
                      ),
                      const SizedBox(width: 16),
                      ElevatedButton(
                        onPressed: _addIngredient,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.blue,
                          foregroundColor: Colors.white,
                          padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                        ),
                        child: const Text('Ekle'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          
          const SizedBox(height: 20),
          
          // İçerik listesi
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(20.0),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(12),
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.withOpacity(0.1),
                  spreadRadius: 1,
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'Mevcut İçerikler',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 16),
                ..._ingredients.map((ingredient) => _buildIngredientCard(ingredient)).toList(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProductCard(Map<String, dynamic> product) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  product['name'],
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: product['status'] == 'Aktif' ? Colors.green : Colors.grey,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  product['status'],
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            product['description'],
            style: TextStyle(
              color: Colors.grey[600],
              fontSize: 14,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Icon(Icons.category, color: Colors.blue, size: 16),
              const SizedBox(width: 4),
              Text(product['category']),
              const SizedBox(width: 16),
              Icon(Icons.attach_money, color: Colors.green, size: 16),
              const SizedBox(width: 4),
              Text('₺${product['price'].toStringAsFixed(2)}'),
            ],
          ),
          if (product['ingredients'] != null) ...[
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: (product['ingredients'] as List<String>).map((ingredient) {
                return Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: Colors.blue[100],
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    ingredient,
                    style: TextStyle(
                      color: Colors.blue[800],
                      fontSize: 12,
                    ),
                  ),
                );
              }).toList(),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildIngredientCard(Map<String, dynamic> ingredient) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  ingredient['name'],
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Icon(Icons.inventory, color: Colors.orange, size: 16),
                    const SizedBox(width: 4),
                    Text('Stok: ${ingredient['currentStock']} ${ingredient['unit']}'),
                    const SizedBox(width: 16),
                    Icon(Icons.warning, color: Colors.red, size: 16),
                    const SizedBox(width: 4),
                    Text('Min: ${ingredient['minStock']} ${ingredient['unit']}'),
                  ],
                ),
              ],
            ),
          ),
          IconButton(
            onPressed: () => _editIngredient(ingredient),
            icon: const Icon(Icons.edit, color: Colors.blue),
          ),
        ],
      ),
    );
  }

  void _addProduct() {
    if (_productFormKey.currentState!.validate()) {
      final newProduct = {
        'id': 'P${(_products.length + 1).toString().padLeft(3, '0')}',
        'name': _productNameController.text,
        'description': _productDescriptionController.text,
        'price': double.tryParse(_productPriceController.text) ?? 0.0,
        'category': _selectedProductCategory,
        'status': _selectedProductStatus,
        'ingredients': [],
      };
      
      setState(() {
        _products.add(newProduct);
      });
      
      _clearProductForm();
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Ürün başarıyla eklendi!'),
          backgroundColor: Color(0xFF4CAF50),
        ),
      );
    }
  }

  void _addIngredient() {
    if (_ingredientFormKey.currentState!.validate()) {
      final newIngredient = {
        'id': 'I${(_ingredients.length + 1).toString().padLeft(3, '0')}',
        'name': _ingredientNameController.text,
        'unit': _units.first,
        'currentStock': double.tryParse(_ingredientStockController.text) ?? 0.0,
        'minStock': double.tryParse(_ingredientMinStockController.text) ?? 0.0,
      };
      
      setState(() {
        _ingredients.add(newIngredient);
      });
      
      _clearIngredientForm();
      
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('İçerik başarıyla eklendi!'),
          backgroundColor: Color(0xFF4CAF50),
        ),
      );
    }
  }

  void _addRecipeIngredient() {
    if (_selectedRecipeIngredient.isNotEmpty && _recipeQuantityController.text.isNotEmpty) {
      // Burada tarif malzemesi eklenebilir
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('${_recipeQuantityController.text} ${_selectedRecipeIngredient} eklendi'),
          backgroundColor: Colors.blue,
        ),
      );
      
      _recipeIngredientController.clear();
      _recipeQuantityController.clear();
      _selectedRecipeIngredient = '';
    }
  }

  void _editIngredient(Map<String, dynamic> ingredient) {
    // Burada içerik düzenleme modalı açılabilir
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('${ingredient['name']} düzenleniyor...'),
        backgroundColor: Colors.blue,
      ),
    );
  }

  void _clearProductForm() {
    _productNameController.clear();
    _productPriceController.clear();
    _productDescriptionController.clear();
    _selectedProductCategory = 'Ana Yemek';
    _selectedProductStatus = 'Aktif';
  }

  void _clearIngredientForm() {
    _ingredientNameController.clear();
    _ingredientStockController.clear();
    _ingredientMinStockController.clear();
  }

  @override
  void dispose() {
    _tabController.dispose();
    _productNameController.dispose();
    _productPriceController.dispose();
    _productDescriptionController.dispose();
    _ingredientNameController.dispose();
    _ingredientUnitController.dispose();
    _ingredientStockController.dispose();
    _ingredientMinStockController.dispose();
    _recipeIngredientController.dispose();
    _recipeQuantityController.dispose();
    super.dispose();
  }
}
